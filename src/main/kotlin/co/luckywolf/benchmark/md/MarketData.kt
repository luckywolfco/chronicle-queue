package co.luckywolf.benchmark.md

import co.luckywolf.benchmark.Command
import co.luckywolf.benchmark.CommandBinary
import co.luckywolf.benchmark.Service
import net.openhft.chronicle.bytes.Bytes
import net.openhft.chronicle.bytes.BytesIn
import net.openhft.chronicle.bytes.BytesOut
import net.openhft.chronicle.bytes.FieldGroup
import net.openhft.chronicle.bytes.util.BinaryLengthLength
import net.openhft.chronicle.core.io.IORuntimeException
import net.openhft.chronicle.wire.*
import java.math.BigDecimal
import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException
import java.util.*


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
            return instrument?: UNDEFINED
        }
    }
}


class DepthItem() : SelfDescribingMarshallable() {
    var timestampNs:Long = 0

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
        wire.write("t").writeLong(timestampNs)
        wire.write("p").writeString(price)
        wire.write("v").writeString(volume)
    }

    override fun readMarshallable(wire: WireIn) {
        timestampNs = wire.read("t").readLong()
        price = wire.read("p").readString()
        volume = wire.read("v").readString()
    }

}

class Price() : SelfDescribingMarshallable() {
    @FieldGroup("price") // 5 longs, each at 8 bytes = 40 bytes, so we can store a String with up to 39 ISO-8859 characters (as the first byte contains the length)
    private val text1: Long = 0

    @FieldGroup("price")
    private val text2: Long = 0

    @FieldGroup("price")
    private val text3: Long = 0

    @FieldGroup("price")
    private val text4: Long = 0

    @FieldGroup("price")
    private val text5: Long = 0

    @FieldGroup("price")
    private val text6: Long = 0

    @FieldGroup("price")
    private val text7: Long = 0

    @FieldGroup("price")
    private val text8: Long = 0

    @Transient
    private val price = Bytes.forFieldGroup(this, "price")

    fun price(price: CharSequence?): Price {
        this.price.append(price)
        return this
    }
}

class Qty() : SelfDescribingMarshallable() {
    @FieldGroup("qty") // 5 longs, each at 8 bytes = 40 bytes, so we can store a String with up to 39 ISO-8859 characters (as the first byte contains the length)
    private val qty1: Long = 0

    @FieldGroup("qty")
    private val qty2: Long = 0

    @FieldGroup("qty")
    private val qty3: Long = 0

    @FieldGroup("qty")
    private val qty4: Long = 0

    @FieldGroup("qty")
    private val qty5: Long = 0

    @FieldGroup("qty")
    private val qty6: Long = 0

    @FieldGroup("qty")
    private val qty7: Long = 0

    @FieldGroup("qty")
    private val qty8: Long = 0

    @Transient
    private val qty = Bytes.forFieldGroup(this, "qty")

    fun qty(qty: CharSequence?): Qty {
        this.qty.append(qty)
        return this
    }
}

//class Item : SelfDescribingMarshallable() {
//    var timestampNs:Long = 0
////    var price: Price? = null
////    var qty: Qty? = null
//    var price:BigDecimal? = null
//    var qty:BigDecimal? = null
//
////    var price:String? = null
////    var qty:String? = null
//
////    override fun writeMarshallable(wire: WireOut) {
////        wire.write("t").writeLong(timestampNs)
////        wire.write("p").writeString(price)
////        wire.write("v").writeString(qty)
////    }
////
////    override fun readMarshallable(wire: WireIn) {
////        timestampNs = wire.read("t").readLong()
////        price = wire.read("p").readString()
////        qty = wire.read("v").readString()
////    }
//}

class Item : BytesInBinaryMarshallable() {
    var timestampNs:Long = 0
    //    var price: Price? = null
//    var qty: Qty? = null
//    var price:BigDecimal = BigDecimal.ZERO
//    var qty:BigDecimal = BigDecimal.ZERO
    var price:String = ""
    var qty:String = ""

    @Override
    @Throws(IORuntimeException::class, BufferUnderflowException::class, IllegalStateException::class)
    override fun readMarshallable(bytes: BytesIn<*>) {
//        timestampNs = bytes.readLong()
        qty = bytes.readUtf8()?:""
        price = bytes.readUtf8()?:""
//        qty = bytes.readBigDecimal()
//        price = bytes.readBigDecimal()
    }

    @Throws(
        IllegalStateException::class,
        BufferOverflowException::class,
        BufferUnderflowException::class,
        ArithmeticException::class
    )
    override fun writeMarshallable(bytes: BytesOut<*>) {
//        bytes.writeLong(timestampNs)
//        bytes.writeBigDecimal(qty)
//        bytes.writeBigDecimal(price)
        bytes.writeUtf8(qty)
        bytes.writeUtf8(price)
    }

    override fun binaryLengthLength(): BinaryLengthLength {
        return BinaryLengthLength.LENGTH_16BIT
    }
}

val emptyTreeMap = TreeMap<BigDecimal, DepthItem>() //Collections.emptySortedMap<BigDecimal,DepthItem>()
class MarketDepth() : Command() {

    constructor(timestampNs: Long,
                instrument: Instrument
    ): this() {
        this.timestampNs = timestampNs
        this.instrument = instrument
    }
    constructor(timestampNs: Long,
                instrument: Instrument,
                bids: TreeMap<BigDecimal, DepthItem>,
                asks: TreeMap<BigDecimal, DepthItem>
    ) : this() {
        this.asks = TreeMap(asks)
        this.bids = TreeMap(bids)
//        bids.forEach() {
//            this.bids[it.key] = it.value
//        }
        this.timestampNs = timestampNs
        this.instrument = instrument
    }

    var asks: TreeMap<BigDecimal, DepthItem> =
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

    override fun writeMarshallable(wire: WireOut) {
        wire.write("ci").writeLong(commandId)
        wire.write("ti").writeLong(traceId)
        wire.write("v").writeInt(version)
        wire.write("o").writeInt(origin.id)
        wire.write("t").writeLong(timestampNs)
        wire.write("i").writeInt(instrument.id)
        wire.write("s").writeInt(service.id)

        wire.write("a").sequence(asks, asks.size, ::writeDepthItems)
        wire.write("b").sequence(bids, bids.size, ::writeDepthItems)
    }

    fun writeDepthItems(depthItems: TreeMap<BigDecimal, DepthItem>, size:Int, valueOut: ValueOut) {
        for (item in depthItems) {
            valueOut.sequence(item.value, ::writeItem)
        }
    }

    fun writeItem(item: DepthItem, valueOut: ValueOut) {
        valueOut.text(item.price)
        valueOut.text(item.volume)
    }

    override fun readMarshallable(wire: WireIn) {
        commandId = wire.read("ci").readLong()
        traceId = wire.read("ti").readLong()
        version = wire.read("v").readInt()
        origin = Service.fromId(wire.read("o").readInt())
        timestampNs = wire.read("t").readLong()
        instrument = Instrument.toInstrument(wire.read("i").readInt())
        service = Service.fromId(wire.read("s").readInt())
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

class MarketDepthArray() : Command() {

    constructor(timestampNs: Long,
                instrument: Instrument
    ): this() {
        this.timestampNs = timestampNs
        this.instrument = instrument
    }
    constructor(timestampNs: Long,
                instrument: Instrument,
                bids: ArrayList<DepthItem>,
                asks: ArrayList<DepthItem>
    ) : this() {
        this
        this.asks = asks
        this.bids = bids
        this.timestampNs = timestampNs
        this.instrument = instrument
    }

    var asks: ArrayList<DepthItem> =
        arrayListOf()
    var bids: ArrayList<DepthItem> =
        arrayListOf()
    var timestampNs: Long = 0
    var instrument: Instrument = Instrument.UNDEFINED
    var service: Service = Service.UNDEFINED

    fun clear() {
        asks.clear()
        bids.clear()
        timestampNs = 0
        instrument = Instrument.UNDEFINED
    }

    override fun writeMarshallable(wire: WireOut) {
        wire.write("ci").writeLong(commandId)
        wire.write("ti").writeLong(traceId)
        wire.write("v").writeInt(version)
        wire.write("o").writeInt(origin.id)
        wire.write("t").writeLong(timestampNs)
        wire.write("i").writeInt(instrument.id)
        wire.write("s").writeInt(service.id)

        wire.write("a").sequence(asks, asks.size, ::writeDepthItems)
        wire.write("b").sequence(bids, bids.size, ::writeDepthItems)
    }

    fun writeDepthItems(depthItems: ArrayList<DepthItem>, size:Int, valueOut: ValueOut) {
        for (i in 0 until depthItems.size) {
            valueOut.sequence(depthItems[i], ::writeItem)
        }
    }

    fun writeItem(item: DepthItem, valueOut: ValueOut) {
        valueOut.writeString(item.price)
        valueOut.writeString(item.volume)
    }

    override fun readMarshallable(wire: WireIn) {
        commandId = wire.read("ci").readLong()
        traceId = wire.read("ti").readLong()
        version = wire.read("v").readInt()
        origin = Service.fromId(wire.read("o").readInt())
        timestampNs = wire.read("t").readLong()
        instrument = Instrument.toInstrument(wire.read("i").readInt())
        service = Service.fromId(wire.read("s").readInt())
        asks.clear()
        bids.clear()

        wire.read("a").sequence(asks, ::readDepthItems)
        wire.read("b").sequence(bids, :: readDepthItems)
    }

    private fun readDepthItems(depthItems: ArrayList<DepthItem>, valueIn: ValueIn) {
        while (valueIn.hasNextSequenceItem()) {
            valueIn.sequence(depthItems, ::readItem)
        }
    }
    val sb = StringBuilder()
    private fun readItem(depthItems: ArrayList<DepthItem>, valueIn: ValueIn) {
        val depthItem = DepthItem() // could maybe use the object pool
        depthItem.price = valueIn.textTo(sb).toString()
        depthItem.volume = valueIn.textTo(sb).toString()
        depthItems.add(depthItem)
    }
}


class MarketDepthBinary() : CommandBinary() {

    constructor(timestampNs: Long,
                instrument: Instrument
    ): this() {
        this.timestampNs = timestampNs
        this.instrument = instrument
    }
    constructor(timestampNs: Long,
                instrument: Instrument,
                bids: ArrayList<Item>,
                asks: ArrayList<Item>
    ) : this() {
        this
        this.asks = asks
        this.bids = bids
        this.timestampNs = timestampNs
        this.instrument = instrument
    }

    var asks: ArrayList<Item> =
        arrayListOf()
    var bids: ArrayList<Item> =
        arrayListOf()
    var timestampNs: Long = 0
    var instrument: Instrument = Instrument.UNDEFINED
    var service: Service = Service.UNDEFINED

    fun clear() {
        asks.clear()
        bids.clear()
        timestampNs = 0
        instrument = Instrument.UNDEFINED
    }

    override fun writeMarshallable(bytes: BytesOut<*>) {
        bytes.writeLong(commandId)
        bytes.writeLong(traceId)
        bytes.writeInt(version)
        bytes.writeInt(origin.id)
//        super.writeMarshallable(bytes)

        bytes.writeInt(service.id)
        bytes.writeLong(timestampNs)
        bytes.writeInt(instrument.id)

        bytes.writeInt(asks.size)
        for (i in 0 until asks.size) {
            val depthItem = asks[i]
            bytes.writeUtf8(depthItem.price)
            bytes.writeUtf8(depthItem.qty)

//            bytes.writeBigDecimal(depthItem.price)
//            bytes.writeBigDecimal(depthItem.qty)
        }

        bytes.writeInt(bids.size)
        for (i in 0 until bids.size) {
            val depthItem = bids[i]
            bytes.writeUtf8(depthItem.price)
            bytes.writeUtf8(depthItem.qty)

//            bytes.writeBigDecimal(depthItem.price)
//            bytes.writeBigDecimal(depthItem.qty)
        }
    }

    override fun readMarshallable(bytes: BytesIn<*>) {
        commandId = bytes.readLong()
        traceId = bytes.readLong()
        version = bytes.readInt()
        origin = Service.fromId(bytes.readInt())
//        super.readMarshallable(bytes)
        service = Service.fromId(bytes.readInt())
        timestampNs = bytes.readLong()
        instrument = Instrument.toInstrument(bytes.readInt())
        asks.clear()
        bids.clear()

//        val sb = StringBuilder()
        val askSize = bytes.readInt()
        for (i in 0 until askSize) {
            val depthItem = Item()
//            depthItem.price = bytes.readBigDecimal()
//            depthItem.qty = bytes.readBigDecimal()
            depthItem.price = bytes.readUtf8()?:""
            depthItem.qty = bytes.readUtf8()?:""
            asks.add(depthItem)
        }

        val bidsSize = bytes.readInt()
        for (i in 0 until bidsSize) {
            val depthItem = Item()
            depthItem.price = bytes.readUtf8()?:""
            depthItem.qty = bytes.readUtf8()?:""
//            depthItem.price = bytes.readBigDecimal()
//            depthItem.qty = bytes.readBigDecimal()
            bids.add(depthItem)
        }
    }
}


class MarketDepthBinary2() : CommandBinary() {

    constructor(timestampNs: Long,
                instrument: Instrument
    ): this() {
        this.timestampNs = timestampNs
        this.instrument = instrument
    }

    var bidPrice1:BigDecimal = BigDecimal.ZERO
    var bidQty1:BigDecimal = BigDecimal.ZERO

    var bidPrice2:BigDecimal = BigDecimal.ZERO
    var bidQty2:BigDecimal = BigDecimal.ZERO

    var bidPrice3:BigDecimal = BigDecimal.ZERO
    var bidQty3:BigDecimal = BigDecimal.ZERO

    var bidPrice4:BigDecimal = BigDecimal.ZERO
    var bidQty4:BigDecimal = BigDecimal.ZERO

    var bidPrice5:BigDecimal = BigDecimal.ZERO
    var bidQty5:BigDecimal = BigDecimal.ZERO

    var bidPrice6:BigDecimal = BigDecimal.ZERO
    var bidQty6:BigDecimal = BigDecimal.ZERO

    var bidPrice7:BigDecimal = BigDecimal.ZERO
    var bidQty7:BigDecimal = BigDecimal.ZERO

    var bidPrice8:BigDecimal = BigDecimal.ZERO
    var bidQty8:BigDecimal = BigDecimal.ZERO

    var bidPrice9:BigDecimal = BigDecimal.ZERO
    var bidQty9:BigDecimal = BigDecimal.ZERO

    var bidPrice10:BigDecimal = BigDecimal.ZERO
    var bidQty10:BigDecimal = BigDecimal.ZERO

    var bidPrice11:BigDecimal = BigDecimal.ZERO
    var bidQty11:BigDecimal = BigDecimal.ZERO

    var bidPrice12:BigDecimal = BigDecimal.ZERO
    var bidQty12:BigDecimal = BigDecimal.ZERO

    var bidPrice13:BigDecimal = BigDecimal.ZERO
    var bidQty13:BigDecimal = BigDecimal.ZERO

    var bidPrice14:BigDecimal = BigDecimal.ZERO
    var bidQty14:BigDecimal = BigDecimal.ZERO

    var bidPrice15:BigDecimal = BigDecimal.ZERO
    var bidQty15:BigDecimal = BigDecimal.ZERO

    var bidPrice16:BigDecimal = BigDecimal.ZERO
    var bidQty16:BigDecimal = BigDecimal.ZERO

    var bidPrice17:BigDecimal = BigDecimal.ZERO
    var bidQty17:BigDecimal = BigDecimal.ZERO

    var bidPrice18:BigDecimal = BigDecimal.ZERO
    var bidQty18:BigDecimal = BigDecimal.ZERO

    var bidPrice19:BigDecimal = BigDecimal.ZERO
    var bidQty19:BigDecimal = BigDecimal.ZERO

    var bidPrice20:BigDecimal = BigDecimal.ZERO
    var bidQty20:BigDecimal = BigDecimal.ZERO

    var bidPrice21:BigDecimal = BigDecimal.ZERO
    var bidQty21:BigDecimal = BigDecimal.ZERO

    var bidPrice22:BigDecimal = BigDecimal.ZERO
    var bidQty22:BigDecimal = BigDecimal.ZERO

    var bidPrice23:BigDecimal = BigDecimal.ZERO
    var bidQty23:BigDecimal = BigDecimal.ZERO

    var bidPrice24:BigDecimal = BigDecimal.ZERO
    var bidQty24:BigDecimal = BigDecimal.ZERO

    var bidPrice25:BigDecimal = BigDecimal.ZERO
    var bidQty25:BigDecimal = BigDecimal.ZERO

    var bidPrice26:BigDecimal = BigDecimal.ZERO
    var bidQty26:BigDecimal = BigDecimal.ZERO

    var bidPrice27:BigDecimal = BigDecimal.ZERO
    var bidQty27:BigDecimal = BigDecimal.ZERO

    var bidPrice28:BigDecimal = BigDecimal.ZERO
    var bidQty28:BigDecimal = BigDecimal.ZERO

    var bidPrice29:BigDecimal = BigDecimal.ZERO
    var bidQty29:BigDecimal = BigDecimal.ZERO

    var bidPrice30:BigDecimal = BigDecimal.ZERO
    var bidQty30:BigDecimal = BigDecimal.ZERO
    
    var bidPrice31:BigDecimal = BigDecimal.ZERO
    var bidQty31:BigDecimal = BigDecimal.ZERO

    var bidPrice32:BigDecimal = BigDecimal.ZERO
    var bidQty32:BigDecimal = BigDecimal.ZERO

    var bidPrice33:BigDecimal = BigDecimal.ZERO
    var bidQty33:BigDecimal = BigDecimal.ZERO

    var bidPrice34:BigDecimal = BigDecimal.ZERO
    var bidQty34:BigDecimal = BigDecimal.ZERO

    var bidPrice35:BigDecimal = BigDecimal.ZERO
    var bidQty35:BigDecimal = BigDecimal.ZERO

    var bidPrice36:BigDecimal = BigDecimal.ZERO
    var bidQty36:BigDecimal = BigDecimal.ZERO

    var bidPrice37:BigDecimal = BigDecimal.ZERO
    var bidQty37:BigDecimal = BigDecimal.ZERO

    var bidPrice38:BigDecimal = BigDecimal.ZERO
    var bidQty38:BigDecimal = BigDecimal.ZERO

    var bidPrice39:BigDecimal = BigDecimal.ZERO
    var bidQty39:BigDecimal = BigDecimal.ZERO

    var bidPrice40:BigDecimal = BigDecimal.ZERO
    var bidQty40:BigDecimal = BigDecimal.ZERO

    var bidPrice41:BigDecimal = BigDecimal.ZERO
    var bidQty41:BigDecimal = BigDecimal.ZERO

    var bidPrice42:BigDecimal = BigDecimal.ZERO
    var bidQty42:BigDecimal = BigDecimal.ZERO

    var bidPrice43:BigDecimal = BigDecimal.ZERO
    var bidQty43:BigDecimal = BigDecimal.ZERO

    var bidPrice44:BigDecimal = BigDecimal.ZERO
    var bidQty44:BigDecimal = BigDecimal.ZERO

    var bidPrice45:BigDecimal = BigDecimal.ZERO
    var bidQty45:BigDecimal = BigDecimal.ZERO

    var bidPrice46:BigDecimal = BigDecimal.ZERO
    var bidQty46:BigDecimal = BigDecimal.ZERO

    var bidPrice47:BigDecimal = BigDecimal.ZERO
    var bidQty47:BigDecimal = BigDecimal.ZERO

    var bidPrice48:BigDecimal = BigDecimal.ZERO
    var bidQty48:BigDecimal = BigDecimal.ZERO

    var bidPrice49:BigDecimal = BigDecimal.ZERO
    var bidQty49:BigDecimal = BigDecimal.ZERO

    var bidPrice50:BigDecimal = BigDecimal.ZERO
    var bidQty50:BigDecimal = BigDecimal.ZERO

    var askPrice1:BigDecimal = BigDecimal.ZERO
    var askQty1:BigDecimal = BigDecimal.ZERO

    var askPrice2:BigDecimal = BigDecimal.ZERO
    var askQty2:BigDecimal = BigDecimal.ZERO

    var askPrice3:BigDecimal = BigDecimal.ZERO
    var askQty3:BigDecimal = BigDecimal.ZERO

    var askPrice4:BigDecimal = BigDecimal.ZERO
    var askQty4:BigDecimal = BigDecimal.ZERO

    var askPrice5:BigDecimal = BigDecimal.ZERO
    var askQty5:BigDecimal = BigDecimal.ZERO

    var askPrice6:BigDecimal = BigDecimal.ZERO
    var askQty6:BigDecimal = BigDecimal.ZERO

    var askPrice7:BigDecimal = BigDecimal.ZERO
    var askQty7:BigDecimal = BigDecimal.ZERO

    var askPrice8:BigDecimal = BigDecimal.ZERO
    var askQty8:BigDecimal = BigDecimal.ZERO

    var askPrice9:BigDecimal = BigDecimal.ZERO
    var askQty9:BigDecimal = BigDecimal.ZERO

    var askPrice10:BigDecimal = BigDecimal.ZERO
    var askQty10:BigDecimal = BigDecimal.ZERO

    var askPrice11:BigDecimal = BigDecimal.ZERO
    var askQty11:BigDecimal = BigDecimal.ZERO

    var askPrice12:BigDecimal = BigDecimal.ZERO
    var askQty12:BigDecimal = BigDecimal.ZERO

    var askPrice13:BigDecimal = BigDecimal.ZERO
    var askQty13:BigDecimal = BigDecimal.ZERO

    var askPrice14:BigDecimal = BigDecimal.ZERO
    var askQty14:BigDecimal = BigDecimal.ZERO

    var askPrice15:BigDecimal = BigDecimal.ZERO
    var askQty15:BigDecimal = BigDecimal.ZERO

    var askPrice16:BigDecimal = BigDecimal.ZERO
    var askQty16:BigDecimal = BigDecimal.ZERO

    var askPrice17:BigDecimal = BigDecimal.ZERO
    var askQty17:BigDecimal = BigDecimal.ZERO

    var askPrice18:BigDecimal = BigDecimal.ZERO
    var askQty18:BigDecimal = BigDecimal.ZERO

    var askPrice19:BigDecimal = BigDecimal.ZERO
    var askQty19:BigDecimal = BigDecimal.ZERO

    var askPrice20:BigDecimal = BigDecimal.ZERO
    var askQty20:BigDecimal = BigDecimal.ZERO

    var askPrice21:BigDecimal = BigDecimal.ZERO
    var askQty21:BigDecimal = BigDecimal.ZERO

    var askPrice22:BigDecimal = BigDecimal.ZERO
    var askQty22:BigDecimal = BigDecimal.ZERO

    var askPrice23:BigDecimal = BigDecimal.ZERO
    var askQty23:BigDecimal = BigDecimal.ZERO

    var askPrice24:BigDecimal = BigDecimal.ZERO
    var askQty24:BigDecimal = BigDecimal.ZERO

    var askPrice25:BigDecimal = BigDecimal.ZERO
    var askQty25:BigDecimal = BigDecimal.ZERO

    var askPrice26:BigDecimal = BigDecimal.ZERO
    var askQty26:BigDecimal = BigDecimal.ZERO

    var askPrice27:BigDecimal = BigDecimal.ZERO
    var askQty27:BigDecimal = BigDecimal.ZERO

    var askPrice28:BigDecimal = BigDecimal.ZERO
    var askQty28:BigDecimal = BigDecimal.ZERO

    var askPrice29:BigDecimal = BigDecimal.ZERO
    var askQty29:BigDecimal = BigDecimal.ZERO

    var askPrice30:BigDecimal = BigDecimal.ZERO
    var askQty30:BigDecimal = BigDecimal.ZERO

    var askPrice31:BigDecimal = BigDecimal.ZERO
    var askQty31:BigDecimal = BigDecimal.ZERO

    var askPrice32:BigDecimal = BigDecimal.ZERO
    var askQty32:BigDecimal = BigDecimal.ZERO

    var askPrice33:BigDecimal = BigDecimal.ZERO
    var askQty33:BigDecimal = BigDecimal.ZERO

    var askPrice34:BigDecimal = BigDecimal.ZERO
    var askQty34:BigDecimal = BigDecimal.ZERO

    var askPrice35:BigDecimal = BigDecimal.ZERO
    var askQty35:BigDecimal = BigDecimal.ZERO

    var askPrice36:BigDecimal = BigDecimal.ZERO
    var askQty36:BigDecimal = BigDecimal.ZERO

    var askPrice37:BigDecimal = BigDecimal.ZERO
    var askQty37:BigDecimal = BigDecimal.ZERO

    var askPrice38:BigDecimal = BigDecimal.ZERO
    var askQty38:BigDecimal = BigDecimal.ZERO

    var askPrice39:BigDecimal = BigDecimal.ZERO
    var askQty39:BigDecimal = BigDecimal.ZERO

    var askPrice40:BigDecimal = BigDecimal.ZERO
    var askQty40:BigDecimal = BigDecimal.ZERO

    var askPrice41:BigDecimal = BigDecimal.ZERO
    var askQty41:BigDecimal = BigDecimal.ZERO

    var askPrice42:BigDecimal = BigDecimal.ZERO
    var askQty42:BigDecimal = BigDecimal.ZERO

    var askPrice43:BigDecimal = BigDecimal.ZERO
    var askQty43:BigDecimal = BigDecimal.ZERO

    var askPrice44:BigDecimal = BigDecimal.ZERO
    var askQty44:BigDecimal = BigDecimal.ZERO

    var askPrice45:BigDecimal = BigDecimal.ZERO
    var askQty45:BigDecimal = BigDecimal.ZERO

    var askPrice46:BigDecimal = BigDecimal.ZERO
    var askQty46:BigDecimal = BigDecimal.ZERO

    var askPrice47:BigDecimal = BigDecimal.ZERO
    var askQty47:BigDecimal = BigDecimal.ZERO

    var askPrice48:BigDecimal = BigDecimal.ZERO
    var askQty48:BigDecimal = BigDecimal.ZERO

    var askPrice49:BigDecimal = BigDecimal.ZERO
    var askQty49:BigDecimal = BigDecimal.ZERO

    var askPrice50:BigDecimal = BigDecimal.ZERO
    var askQty50:BigDecimal = BigDecimal.ZERO
    
    var timestampNs: Long = 0
    var instrument: Instrument = Instrument.UNDEFINED
    var service: Service = Service.UNDEFINED
    

    override fun writeMarshallable(bytes: BytesOut<*>) {
        super.writeMarshallable(bytes)
        bytes.writeInt(service.id)
        bytes.writeLong(timestampNs)
        bytes.writeInt(instrument.id)

        bytes.writeBigDecimal(askQty1)
        bytes.writeBigDecimal(askPrice1)

        bytes.writeBigDecimal(askQty2)
        bytes.writeBigDecimal(askPrice2)

        bytes.writeBigDecimal(askQty3)
        bytes.writeBigDecimal(askPrice3)

        bytes.writeBigDecimal(askQty4)
        bytes.writeBigDecimal(askPrice4)

        bytes.writeBigDecimal(askQty5)
        bytes.writeBigDecimal(askPrice5)
        
        bytes.writeBigDecimal(askQty6)
        bytes.writeBigDecimal(askPrice6)

        bytes.writeBigDecimal(askQty7)
        bytes.writeBigDecimal(askPrice7)

        bytes.writeBigDecimal(askQty8)
        bytes.writeBigDecimal(askPrice8)

        bytes.writeBigDecimal(askQty9)
        bytes.writeBigDecimal(askPrice9)

        bytes.writeBigDecimal(askQty10)
        bytes.writeBigDecimal(askPrice10)

        bytes.writeBigDecimal(askQty11)
        bytes.writeBigDecimal(askPrice11)

        bytes.writeBigDecimal(askQty12)
        bytes.writeBigDecimal(askPrice12)

        bytes.writeBigDecimal(askQty13)
        bytes.writeBigDecimal(askPrice13)

        bytes.writeBigDecimal(askQty14)
        bytes.writeBigDecimal(askPrice14)

        bytes.writeBigDecimal(askQty15)
        bytes.writeBigDecimal(askPrice15)

        bytes.writeBigDecimal(askQty16)
        bytes.writeBigDecimal(askPrice16)

        bytes.writeBigDecimal(askQty17)
        bytes.writeBigDecimal(askPrice17)

        bytes.writeBigDecimal(askQty18)
        bytes.writeBigDecimal(askPrice18)

        bytes.writeBigDecimal(askQty19)
        bytes.writeBigDecimal(askPrice19)

        bytes.writeBigDecimal(askQty20)
        bytes.writeBigDecimal(askPrice20)

        bytes.writeBigDecimal(askQty21)
        bytes.writeBigDecimal(askPrice21)

        bytes.writeBigDecimal(askQty22)
        bytes.writeBigDecimal(askPrice22)

        bytes.writeBigDecimal(askQty23)
        bytes.writeBigDecimal(askPrice23)

        bytes.writeBigDecimal(askQty24)
        bytes.writeBigDecimal(askPrice24)

        bytes.writeBigDecimal(askQty25)
        bytes.writeBigDecimal(askPrice25)

        bytes.writeBigDecimal(askQty26)
        bytes.writeBigDecimal(askPrice26)

        bytes.writeBigDecimal(askQty27)
        bytes.writeBigDecimal(askPrice27)

        bytes.writeBigDecimal(askQty28)
        bytes.writeBigDecimal(askPrice28)

        bytes.writeBigDecimal(askQty29)
        bytes.writeBigDecimal(askPrice29)

        bytes.writeBigDecimal(askQty30)
        bytes.writeBigDecimal(askPrice30)

        bytes.writeBigDecimal(askQty31)
        bytes.writeBigDecimal(askPrice31)

        bytes.writeBigDecimal(askQty32)
        bytes.writeBigDecimal(askPrice32)

        bytes.writeBigDecimal(askQty33)
        bytes.writeBigDecimal(askPrice33)

        bytes.writeBigDecimal(askQty34)
        bytes.writeBigDecimal(askPrice34)

        bytes.writeBigDecimal(askQty35)
        bytes.writeBigDecimal(askPrice35)

        bytes.writeBigDecimal(askQty36)
        bytes.writeBigDecimal(askPrice36)

        bytes.writeBigDecimal(askQty37)
        bytes.writeBigDecimal(askPrice37)

        bytes.writeBigDecimal(askQty38)
        bytes.writeBigDecimal(askPrice38)

        bytes.writeBigDecimal(askQty39)
        bytes.writeBigDecimal(askPrice39)

        bytes.writeBigDecimal(askQty40)
        bytes.writeBigDecimal(askPrice40)

        bytes.writeBigDecimal(askQty41)
        bytes.writeBigDecimal(askPrice41)

        bytes.writeBigDecimal(askQty42)
        bytes.writeBigDecimal(askPrice42)

        bytes.writeBigDecimal(askQty43)
        bytes.writeBigDecimal(askPrice43)

        bytes.writeBigDecimal(askQty44)
        bytes.writeBigDecimal(askPrice44)

        bytes.writeBigDecimal(askQty45)
        bytes.writeBigDecimal(askPrice45)

        bytes.writeBigDecimal(askQty46)
        bytes.writeBigDecimal(askPrice46)

        bytes.writeBigDecimal(askQty47)
        bytes.writeBigDecimal(askPrice47)

        bytes.writeBigDecimal(askQty48)
        bytes.writeBigDecimal(askPrice48)

        bytes.writeBigDecimal(askQty49)
        bytes.writeBigDecimal(askPrice49)

        bytes.writeBigDecimal(askQty50)
        bytes.writeBigDecimal(askPrice50)

        bytes.writeBigDecimal(bidQty1)
        bytes.writeBigDecimal(bidPrice1)

        bytes.writeBigDecimal(bidQty2)
        bytes.writeBigDecimal(bidPrice2)

        bytes.writeBigDecimal(bidQty3)
        bytes.writeBigDecimal(bidPrice3)

        bytes.writeBigDecimal(bidQty4)
        bytes.writeBigDecimal(bidPrice4)

        bytes.writeBigDecimal(bidQty5)
        bytes.writeBigDecimal(bidPrice5)

        bytes.writeBigDecimal(bidQty6)
        bytes.writeBigDecimal(bidPrice6)

        bytes.writeBigDecimal(bidQty7)
        bytes.writeBigDecimal(bidPrice7)

        bytes.writeBigDecimal(bidQty8)
        bytes.writeBigDecimal(bidPrice8)

        bytes.writeBigDecimal(bidQty9)
        bytes.writeBigDecimal(bidPrice9)

        bytes.writeBigDecimal(bidQty10)
        bytes.writeBigDecimal(bidPrice10)

        bytes.writeBigDecimal(bidQty11)
        bytes.writeBigDecimal(bidPrice11)

        bytes.writeBigDecimal(bidQty12)
        bytes.writeBigDecimal(bidPrice12)

        bytes.writeBigDecimal(bidQty13)
        bytes.writeBigDecimal(bidPrice13)

        bytes.writeBigDecimal(bidQty14)
        bytes.writeBigDecimal(bidPrice14)

        bytes.writeBigDecimal(bidQty15)
        bytes.writeBigDecimal(bidPrice15)

        bytes.writeBigDecimal(bidQty16)
        bytes.writeBigDecimal(bidPrice16)

        bytes.writeBigDecimal(bidQty17)
        bytes.writeBigDecimal(bidPrice17)

        bytes.writeBigDecimal(bidQty18)
        bytes.writeBigDecimal(bidPrice18)

        bytes.writeBigDecimal(bidQty19)
        bytes.writeBigDecimal(bidPrice19)

        bytes.writeBigDecimal(bidQty20)
        bytes.writeBigDecimal(bidPrice20)

        bytes.writeBigDecimal(bidQty21)
        bytes.writeBigDecimal(bidPrice21)

        bytes.writeBigDecimal(bidQty22)
        bytes.writeBigDecimal(bidPrice22)

        bytes.writeBigDecimal(bidQty23)
        bytes.writeBigDecimal(bidPrice23)

        bytes.writeBigDecimal(bidQty24)
        bytes.writeBigDecimal(bidPrice24)

        bytes.writeBigDecimal(bidQty25)
        bytes.writeBigDecimal(bidPrice25)

        bytes.writeBigDecimal(bidQty26)
        bytes.writeBigDecimal(bidPrice26)

        bytes.writeBigDecimal(bidQty27)
        bytes.writeBigDecimal(bidPrice27)

        bytes.writeBigDecimal(bidQty28)
        bytes.writeBigDecimal(bidPrice28)

        bytes.writeBigDecimal(bidQty29)
        bytes.writeBigDecimal(bidPrice29)

        bytes.writeBigDecimal(bidQty30)
        bytes.writeBigDecimal(bidPrice30)

        bytes.writeBigDecimal(bidQty31)
        bytes.writeBigDecimal(bidPrice31)

        bytes.writeBigDecimal(bidQty32)
        bytes.writeBigDecimal(bidPrice32)

        bytes.writeBigDecimal(bidQty33)
        bytes.writeBigDecimal(bidPrice33)

        bytes.writeBigDecimal(bidQty34)
        bytes.writeBigDecimal(bidPrice34)

        bytes.writeBigDecimal(bidQty35)
        bytes.writeBigDecimal(bidPrice35)

        bytes.writeBigDecimal(bidQty36)
        bytes.writeBigDecimal(bidPrice36)

        bytes.writeBigDecimal(bidQty37)
        bytes.writeBigDecimal(bidPrice37)

        bytes.writeBigDecimal(bidQty38)
        bytes.writeBigDecimal(bidPrice38)

        bytes.writeBigDecimal(bidQty39)
        bytes.writeBigDecimal(bidPrice39)

        bytes.writeBigDecimal(bidQty40)
        bytes.writeBigDecimal(bidPrice40)

        bytes.writeBigDecimal(bidQty41)
        bytes.writeBigDecimal(bidPrice41)

        bytes.writeBigDecimal(bidQty42)
        bytes.writeBigDecimal(bidPrice42)

        bytes.writeBigDecimal(bidQty43)
        bytes.writeBigDecimal(bidPrice43)

        bytes.writeBigDecimal(bidQty44)
        bytes.writeBigDecimal(bidPrice44)

        bytes.writeBigDecimal(bidQty45)
        bytes.writeBigDecimal(bidPrice45)

        bytes.writeBigDecimal(bidQty46)
        bytes.writeBigDecimal(bidPrice46)

        bytes.writeBigDecimal(bidQty47)
        bytes.writeBigDecimal(bidPrice47)

        bytes.writeBigDecimal(bidQty48)
        bytes.writeBigDecimal(bidPrice48)

        bytes.writeBigDecimal(bidQty49)
        bytes.writeBigDecimal(bidPrice49)

        bytes.writeBigDecimal(bidQty50)
        bytes.writeBigDecimal(bidPrice50)
    }

    override fun readMarshallable(bytes: BytesIn<*>) {
        super.readMarshallable(bytes)
        service = Service.fromId(bytes.readInt())
        timestampNs = bytes.readLong()
        instrument = Instrument.toInstrument(bytes.readInt())

        askQty1 = bytes.readBigDecimal()
        askPrice1 = bytes.readBigDecimal()

        askQty2 = bytes.readBigDecimal()
        askPrice2 = bytes.readBigDecimal()

        askQty3 = bytes.readBigDecimal()
        askPrice3 = bytes.readBigDecimal()

        askQty4 = bytes.readBigDecimal()
        askPrice4 = bytes.readBigDecimal()

        askQty5 = bytes.readBigDecimal()
        askPrice5 = bytes.readBigDecimal()

        askQty6 = bytes.readBigDecimal()
        askPrice6 = bytes.readBigDecimal()

        askQty7 = bytes.readBigDecimal()
        askPrice7 = bytes.readBigDecimal()

        askQty8 = bytes.readBigDecimal()
        askPrice8 = bytes.readBigDecimal()

        askQty9 = bytes.readBigDecimal()
        askPrice9 = bytes.readBigDecimal()

        askQty10 = bytes.readBigDecimal()
        askPrice10 = bytes.readBigDecimal()

        askQty11 = bytes.readBigDecimal()
        askPrice11 = bytes.readBigDecimal()

        askQty12 = bytes.readBigDecimal()
        askPrice12 = bytes.readBigDecimal()

        askQty13 = bytes.readBigDecimal()
        askPrice13 = bytes.readBigDecimal()

        askQty14 = bytes.readBigDecimal()
        askPrice14 = bytes.readBigDecimal()

        askQty15 = bytes.readBigDecimal()
        askPrice15 = bytes.readBigDecimal()

        askQty16 = bytes.readBigDecimal()
        askPrice16 = bytes.readBigDecimal()

        askQty17 = bytes.readBigDecimal()
        askPrice17 = bytes.readBigDecimal()

        askQty18 = bytes.readBigDecimal()
        askPrice18 = bytes.readBigDecimal()

        askQty19 = bytes.readBigDecimal()
        askPrice19 = bytes.readBigDecimal()

        askQty20 = bytes.readBigDecimal()
        askPrice20 = bytes.readBigDecimal()

        askQty21 = bytes.readBigDecimal()
        askPrice21 = bytes.readBigDecimal()

        askQty22 = bytes.readBigDecimal()
        askPrice22 = bytes.readBigDecimal()

        askQty23 = bytes.readBigDecimal()
        askPrice23 = bytes.readBigDecimal()

        askQty24 = bytes.readBigDecimal()
        askPrice24 = bytes.readBigDecimal()

        askQty25 = bytes.readBigDecimal()
        askPrice25 = bytes.readBigDecimal()

        askQty26 = bytes.readBigDecimal()
        askPrice26 = bytes.readBigDecimal()

        askQty27 = bytes.readBigDecimal()
        askPrice27 = bytes.readBigDecimal()

        askQty28 = bytes.readBigDecimal()
        askPrice28 = bytes.readBigDecimal()

        askQty29 = bytes.readBigDecimal()
        askPrice29 = bytes.readBigDecimal()

        askQty30 = bytes.readBigDecimal()
        askPrice30 = bytes.readBigDecimal()

        askQty31 = bytes.readBigDecimal()
        askPrice31 = bytes.readBigDecimal()

        askQty32 = bytes.readBigDecimal()
        askPrice32 = bytes.readBigDecimal()

        askQty33 = bytes.readBigDecimal()
        askPrice33 = bytes.readBigDecimal()

        askQty34 = bytes.readBigDecimal()
        askPrice34 = bytes.readBigDecimal()

        askQty35 = bytes.readBigDecimal()
        askPrice35 = bytes.readBigDecimal()

        askQty36 = bytes.readBigDecimal()
        askPrice36 = bytes.readBigDecimal()

        askQty37 = bytes.readBigDecimal()
        askPrice37 = bytes.readBigDecimal()

        askQty38 = bytes.readBigDecimal()
        askPrice38 = bytes.readBigDecimal()

        askQty39 = bytes.readBigDecimal()
        askPrice39 = bytes.readBigDecimal()

        askQty40 = bytes.readBigDecimal()
        askPrice40 = bytes.readBigDecimal()

        askQty41 = bytes.readBigDecimal()
        askPrice41 = bytes.readBigDecimal()

        askQty42 = bytes.readBigDecimal()
        askPrice42 = bytes.readBigDecimal()

        askQty43 = bytes.readBigDecimal()
        askPrice43 = bytes.readBigDecimal()

        askQty44 = bytes.readBigDecimal()
        askPrice44 = bytes.readBigDecimal()

        askQty45 = bytes.readBigDecimal()
        askPrice45 = bytes.readBigDecimal()

        askQty46 = bytes.readBigDecimal()
        askPrice46 = bytes.readBigDecimal()

        askQty47 = bytes.readBigDecimal()
        askPrice47 = bytes.readBigDecimal()

        askQty48 = bytes.readBigDecimal()
        askPrice48 = bytes.readBigDecimal()

        askQty49 = bytes.readBigDecimal()
        askPrice49 = bytes.readBigDecimal()

        askQty50 = bytes.readBigDecimal()
        askPrice50 = bytes.readBigDecimal()

        bidQty1 = bytes.readBigDecimal()
        bidPrice1 = bytes.readBigDecimal()

        bidQty2 = bytes.readBigDecimal()
        bidPrice2 = bytes.readBigDecimal()

        bidQty3 = bytes.readBigDecimal()
        bidPrice3 = bytes.readBigDecimal()

        bidQty4 = bytes.readBigDecimal()
        bidPrice4 = bytes.readBigDecimal()

        bidQty5 = bytes.readBigDecimal()
        bidPrice5 = bytes.readBigDecimal()

        bidQty6 = bytes.readBigDecimal()
        bidPrice6 = bytes.readBigDecimal()

        bidQty7 = bytes.readBigDecimal()
        bidPrice7 = bytes.readBigDecimal()

        bidQty8 = bytes.readBigDecimal()
        bidPrice8 = bytes.readBigDecimal()

        bidQty9 = bytes.readBigDecimal()
        bidPrice9 = bytes.readBigDecimal()

        bidQty10 = bytes.readBigDecimal()
        bidPrice10 = bytes.readBigDecimal()

        bidQty11 = bytes.readBigDecimal()
        bidPrice11 = bytes.readBigDecimal()

        bidQty12 = bytes.readBigDecimal()
        bidPrice12 = bytes.readBigDecimal()

        bidQty13 = bytes.readBigDecimal()
        bidPrice13 = bytes.readBigDecimal()

        bidQty14 = bytes.readBigDecimal()
        bidPrice14 = bytes.readBigDecimal()

        bidQty15 = bytes.readBigDecimal()
        bidPrice15 = bytes.readBigDecimal()

        bidQty16 = bytes.readBigDecimal()
        bidPrice16 = bytes.readBigDecimal()

        bidQty17 = bytes.readBigDecimal()
        bidPrice17 = bytes.readBigDecimal()

        bidQty18 = bytes.readBigDecimal()
        bidPrice18 = bytes.readBigDecimal()

        bidQty19 = bytes.readBigDecimal()
        bidPrice19 = bytes.readBigDecimal()

        bidQty20 = bytes.readBigDecimal()
        bidPrice20 = bytes.readBigDecimal()

        bidQty21 = bytes.readBigDecimal()
        bidPrice21 = bytes.readBigDecimal()

        bidQty22 = bytes.readBigDecimal()
        bidPrice22 = bytes.readBigDecimal()

        bidQty23 = bytes.readBigDecimal()
        bidPrice23 = bytes.readBigDecimal()

        bidQty24 = bytes.readBigDecimal()
        bidPrice24 = bytes.readBigDecimal()

        bidQty25 = bytes.readBigDecimal()
        bidPrice25 = bytes.readBigDecimal()

        bidQty26 = bytes.readBigDecimal()
        bidPrice26 = bytes.readBigDecimal()

        bidQty27 = bytes.readBigDecimal()
        bidPrice27 = bytes.readBigDecimal()

        bidQty28 = bytes.readBigDecimal()
        bidPrice28 = bytes.readBigDecimal()

        bidQty29 = bytes.readBigDecimal()
        bidPrice29 = bytes.readBigDecimal()

        bidQty30 = bytes.readBigDecimal()
        bidPrice30 = bytes.readBigDecimal()

        bidQty31 = bytes.readBigDecimal()
        bidPrice31 = bytes.readBigDecimal()

        bidQty32 = bytes.readBigDecimal()
        bidPrice32 = bytes.readBigDecimal()

        bidQty33 = bytes.readBigDecimal()
        bidPrice33 = bytes.readBigDecimal()

        bidQty34 = bytes.readBigDecimal()
        bidPrice34 = bytes.readBigDecimal()

        bidQty35 = bytes.readBigDecimal()
        bidPrice35 = bytes.readBigDecimal()

        bidQty36 = bytes.readBigDecimal()
        bidPrice36 = bytes.readBigDecimal()

        bidQty37 = bytes.readBigDecimal()
        bidPrice37 = bytes.readBigDecimal()

        bidQty38 = bytes.readBigDecimal()
        bidPrice38 = bytes.readBigDecimal()

        bidQty39 = bytes.readBigDecimal()
        bidPrice39 = bytes.readBigDecimal()

        bidQty40 = bytes.readBigDecimal()
        bidPrice40 = bytes.readBigDecimal()

        bidQty41 = bytes.readBigDecimal()
        bidPrice41 = bytes.readBigDecimal()

        bidQty42 = bytes.readBigDecimal()
        bidPrice42 = bytes.readBigDecimal()

        bidQty43 = bytes.readBigDecimal()
        bidPrice43 = bytes.readBigDecimal()

        bidQty44 = bytes.readBigDecimal()
        bidPrice44 = bytes.readBigDecimal()

        bidQty45 = bytes.readBigDecimal()
        bidPrice45 = bytes.readBigDecimal()

        bidQty46 = bytes.readBigDecimal()
        bidPrice46 = bytes.readBigDecimal()

        bidQty47 = bytes.readBigDecimal()
        bidPrice47 = bytes.readBigDecimal()

        bidQty48 = bytes.readBigDecimal()
        bidPrice48 = bytes.readBigDecimal()

        bidQty49 = bytes.readBigDecimal()
        bidPrice49 = bytes.readBigDecimal()

        bidQty50 = bytes.readBigDecimal()
        bidPrice50 = bytes.readBigDecimal()

    }
}


class BinaryDepthItem : BytesInBinaryMarshallable(), Comparable<BinaryDepthItem> {
    var timestampNs:Long = 0
    //    var price: Price? = null
//    var qty: Qty? = null
    var price:BigDecimal = BigDecimal.ZERO
    var volume:BigDecimal = BigDecimal.ZERO

//    var price:String = ""
//    var qty:String = ""

    override fun compareTo(other: BinaryDepthItem): Int {
        return this.price.compareTo(other.price)
    }

    @Override
    @Throws(IORuntimeException::class, BufferUnderflowException::class, IllegalStateException::class)
    override fun readMarshallable(bytes: BytesIn<*>) {
        timestampNs = bytes.readLong()
        volume = bytes.readBigDecimal()
        price = bytes.readBigDecimal()
    }

    @Throws(
        IllegalStateException::class,
        BufferOverflowException::class,
        BufferUnderflowException::class,
        ArithmeticException::class
    )
    override fun writeMarshallable(bytes: BytesOut<*>) {
        bytes.writeLong(timestampNs)
        bytes.writeBigDecimal(volume)
        bytes.writeBigDecimal(price)
    }

    override fun binaryLengthLength(): BinaryLengthLength {
        return BinaryLengthLength.LENGTH_16BIT
    }
}


val emptySet = TreeSet<BinaryDepthItem>() //Collections.emptySortedMap<BigDecimal,DepthItem>()
class MarketDepthSet() : Command() {

    constructor(timestampNs: Long,
                instrument: Instrument
    ): this() {
        this.timestampNs = timestampNs
        this.instrument = instrument
    }
    constructor(timestampNs: Long,
                instrument: Instrument,
                bids: TreeSet<BinaryDepthItem>,
                asks: TreeSet<BinaryDepthItem>
    ) : this() {
        this.asks = TreeSet(asks)
        this.bids = TreeSet(bids)
//        bids.forEach() {
//            this.bids[it.key] = it.value
//        }
        this.timestampNs = timestampNs
        this.instrument = instrument
    }

    var asks: TreeSet<BinaryDepthItem> =
        TreeSet()
    var bids: TreeSet<BinaryDepthItem> =
        TreeSet(Collections.reverseOrder())
    var timestampNs: Long = 0
    var instrument: Instrument = Instrument.UNDEFINED
    var service: Service = Service.UNDEFINED

    fun clear() {
        asks.clear()
        bids.clear()
        timestampNs = 0
        instrument = Instrument.UNDEFINED
    }


    override fun writeMarshallable(wire: WireOut) {
        wire.write("ci").writeLong(commandId)
        wire.write("ti").writeLong(traceId)
        wire.write("v").writeInt(version)
        wire.write("o").writeInt(origin.id)
        wire.write("t").writeLong(timestampNs)
        wire.write("i").writeInt(instrument.id)
        wire.write("s").writeInt(service.id)

        wire.write("a").sequence(asks, asks.size, ::writeDepthItems)
        wire.write("b").sequence(bids, bids.size, ::writeDepthItems)
    }

    fun writeDepthItems(depthItems: TreeSet<BinaryDepthItem>, size:Int, valueOut: ValueOut) {
        for (item in depthItems) {
            valueOut.sequence(item, ::writeItem)
        }
    }

    fun writeItem(item: BinaryDepthItem, valueOut: ValueOut) {
        valueOut.wireOut().bytes().writeBigDecimal(item.price)
        valueOut.wireOut().bytes().writeBigDecimal(item.volume)
    }

    override fun readMarshallable(wire: WireIn) {
        commandId = wire.read("ci").readLong()
        traceId = wire.read("ti").readLong()
        version = wire.read("v").readInt()
        origin = Service.fromId(wire.read("o").readInt())
        timestampNs = wire.read("t").readLong()
        instrument = Instrument.toInstrument(wire.read("i").readInt())
        service = Service.fromId(wire.read("s").readInt())
        asks.clear()
        bids.clear()
        wire.read("a").sequence(asks, ::readDepthItems)
        wire.read("b").sequence(bids, :: readDepthItems)
    }

    private fun readDepthItems(depthItems: TreeSet<BinaryDepthItem>, valueIn: ValueIn) {
        while (valueIn.hasNextSequenceItem()) {
            valueIn.sequence(depthItems, ::readItem)
        }
    }

    private fun readItem(depthItems: TreeSet<BinaryDepthItem>, valueIn: ValueIn) {
        val depthItem = BinaryDepthItem() // could maybe use the object pool
        depthItem.price = valueIn.wireIn().bytes().readBigDecimal()
        depthItem.volume = valueIn.wireIn().bytes().readBigDecimal()
        depthItems.add(depthItem)
    }
}






