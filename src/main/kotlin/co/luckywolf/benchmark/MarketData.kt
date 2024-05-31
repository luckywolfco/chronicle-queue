package co.luckywolf.benchmark

import net.openhft.chronicle.wire.SelfDescribingMarshallable
import net.openhft.chronicle.wire.ValueIn
import net.openhft.chronicle.wire.ValueOut
import net.openhft.chronicle.wire.WireIn
import net.openhft.chronicle.wire.WireOut
import java.math.BigDecimal
import java.util.Collections
import java.util.TreeMap

val MINUS_ONE_BD = BigDecimal.ONE.negate()

enum class Side {
    BUY {
        override fun direction() = 1.0
        override fun directionBd(): BigDecimal = BigDecimal.ONE
        override fun isBuy(): Boolean = true
        override fun invert(): Side = SELL
        override fun opposite(): Side = SELL
        override fun isOpposite(side: Side): Boolean {
            return side.isSell()
        }
    },
    SELL {
        override fun direction(): Double = -1.0
        override fun directionBd(): BigDecimal = MINUS_ONE_BD
        override fun isSell() = true
        override fun invert(): Side = BUY
        override fun opposite(): Side = BUY
        override fun isOpposite(side: Side): Boolean {
            return side.isBuy()
        }
    },
    NONE {
        override fun direction(): Double = 0.0
        override fun directionBd(): BigDecimal = BigDecimal.ZERO
        override fun invert(): Side = NONE
        override fun opposite(): Side = NONE
        override fun isOpposite(side: Side): Boolean = false
        override fun isNone(): Boolean = true
    };

    abstract fun direction(): Double
    abstract fun directionBd(): BigDecimal

    abstract fun opposite(): Side

    abstract fun invert(): Side

    abstract fun isOpposite(side: Side): Boolean

    open fun isBuy(): Boolean = false

    open fun isSell(): Boolean = false

    open fun isNone(): Boolean = false

    companion object {
        fun valueOf(sb: StringBuilder?): Side {
            if (sb != null) {
                if (sb.startsWith("buy", true)) {
                    return BUY
                }
                if (sb.startsWith("sell", true)) {
                    return SELL
                }
            }
            return NONE
        }
    }
}
enum class Exchange(val id: Int) {
    UNDEFINED(0),
    BINANCE(1),
    VALR(2);

    companion object {

        fun exchangeFromId(id: Int) : Exchange {
            if (VALR.id == id) {
                return VALR
            }
            if (BINANCE.id == id) {
                return BINANCE
            }
            return UNDEFINED
        }

        fun marketDataServiceFor(exchange: Exchange): Service {
            if (VALR == exchange) {
                return Service.VALR_SOURCE_MARKET_DATA
            }
            if (BINANCE == exchange) {
                return Service.BINANCE_SOURCE_MARKET_DATA
            }
            return Service.UNDEFINED
        }
    }
}

enum class CurrencyPair() {
    UNDEFINED,
    ETHZAR,
    BTCZAR
    ;
}

enum class Instrument(
    val id: Int,
    val exchange: Exchange,
    val currencyPair: CurrencyPair,
    val exchangeSymbol: String
) {

    UNDEFINED(0, Exchange.UNDEFINED, CurrencyPair.UNDEFINED, "UNDEFINED"),
    VALR_ETH_ZAR(2, Exchange.VALR, CurrencyPair.ETHZAR, "ETHZAR"),
    VALR_BTC_ZAR(3, Exchange.VALR, CurrencyPair.BTCZAR, "BTCZAR");

    fun symbol(): String {
        return this.exchangeSymbol
    }

    companion object {
        private val exchangeIdMap = Instrument.values().map { it.id to it  }.toMap()

        fun toInstrument(id: Int): Instrument {
            val instrument = exchangeIdMap[id]
            return instrument?:UNDEFINED
        }
    }
}


class DepthItem() : SelfDescribingMarshallable() {
    var price: String = "0"
        set(value) {
            field = value
            priceBigDecimal = null
        }
    var volume: String = "0"
        set(value) {
            field = value
            volumeBigDecimal = null
        }

    private var priceBigDecimal: BigDecimal? = null

    private var volumeBigDecimal: BigDecimal? = null

    fun volumeBigDecimal(): BigDecimal {
        if (volumeBigDecimal == null) {
            volumeBigDecimal = volume.toBigDecimal()
        }
        return volumeBigDecimal!!
    }

    fun priceBigDecimal(): BigDecimal {
        if (priceBigDecimal == null) {
            priceBigDecimal = price.toBigDecimal()
        }
        return priceBigDecimal!!
    }

    constructor(price: String, volume: String) : this() {
        this.price = price
        this.volume = volume
    }

    constructor(price: BigDecimal, volume: BigDecimal) : this() {
        this.price = price.toPlainString()
        this.volume = volume.toPlainString()
    }

    override fun writeMarshallable(wire: WireOut) {
        wire.write("p").writeString(price)
        wire.write("v").writeString(volume)
    }

    override fun readMarshallable(wire: WireIn) {
        price = wire.read("p").readString()
        volume = wire.read("v").readString()
    }

}

val emptyTreeMap = TreeMap<BigDecimal,DepthItem>() //Collections.emptySortedMap<BigDecimal,DepthItem>()
class MarketDepth() : Command() {

    constructor(timestampNs: Long,
                instrument: Instrument): this() {
        this.timestampNs = timestampNs
        this.instrument = instrument
    }
    constructor(timestampNs: Long,
                instrument: Instrument,
                bids: TreeMap<BigDecimal, DepthItem>,
                asks: TreeMap<BigDecimal, DepthItem>
    ) : this() {
        this.asks = TreeMap(asks)
        this.bids = TreeMap(Collections.reverseOrder())
        bids.forEach() {
            this.bids[it.key] = it.value
        }
        this.timestampNs = timestampNs
        this.instrument = instrument
    }

    var asks: TreeMap<BigDecimal,DepthItem> =
        TreeMap()
    var bids: TreeMap<BigDecimal, DepthItem> =
        TreeMap(Collections.reverseOrder())
    var timestampNs: Long = 0
    var instrument: Instrument = Instrument.UNDEFINED
    var service: Service = Service.UNDEFINED

    fun clear() {
        asks.clear()
        bids.clear()
        timestampNs = 0
        instrument = Instrument.UNDEFINED
    }

    fun items(side: Side): TreeMap<BigDecimal, DepthItem> {
        return if (side.isBuy()) bids else if (side.isSell()) asks else emptyTreeMap
    }

    override fun writeMarshallable(wire: WireOut) {
        wire.write("ci").writeLong(commandId)
        wire.write("ti").writeLong(traceId)
        wire.write("v").writeInt(version)
        wire.write("o").writeInt(origin.ordinal)
        wire.write("t").writeLong(timestampNs)
        wire.write("i").writeInt(instrument.id)
        wire.write("s").writeInt(service.ordinal)

        wire.write("a").sequence(asks, ::writeDepthItems)
        wire.write("b").sequence(bids, ::writeDepthItems)
    }

    fun writeDepthItems(depthItems: TreeMap<BigDecimal, DepthItem>, valueOut: ValueOut) {
        for (item in depthItems) {
            valueOut.sequence(item.value, ::writeItem)
        }
    }

    fun writeItem(item: DepthItem, valueOut: ValueOut) {
        valueOut.text(item.price)
        valueOut.text(item.volume)
    }

    private val summaryString = StringBuilder()
    fun summary() : String {
        summaryString.clear()
        summaryString.append("Market depth for $instrument\n")
        summaryString.append("Timestamp in ns is $timestampNs\n")
        summaryString.append("best ask = ")
        if (asks.isNotEmpty())
            summaryString.append(asks.firstEntry().value)
        else
            summaryString.append("[]\n")
        summaryString.append("best bid = ")
        if (bids.isNotEmpty())
             summaryString.append(bids.firstEntry().value)
        else
            summaryString.append("[]\n")
        return summaryString.toString()
    }

    override fun readMarshallable(wire: WireIn) {
        commandId = wire.read("ci").readLong()
        traceId = wire.read("ti").readLong()
        version = wire.read("v").readInt()
        origin = Service.values()[wire.read("o").readInt()]
        timestampNs = wire.read("t").readLong()
        instrument = Instrument.toInstrument(wire.read("i").readInt())
        service = Service.values()[wire.read("s").readInt()]
        asks.clear()
        bids.clear()
        wire.read("a").sequence(asks, ::readDepthItems)
        wire.read("b").sequence(bids, :: readDepthItems)
    }

    private fun readDepthItems(depthItems: TreeMap<BigDecimal, DepthItem>, valueIn: ValueIn) {
        while (valueIn.hasNextSequenceItem()) {
            valueIn.sequence(depthItems, ::readItem)
        }
    }

    private fun readItem(depthItems: TreeMap<BigDecimal, DepthItem>, valueIn: ValueIn) {
        val depthItem = DepthItem() // could maybe use the object pool
        depthItem.price = valueIn.text()!!
        depthItem.volume = valueIn.text()!!
        depthItems[depthItem.priceBigDecimal()] = depthItem
    }
}