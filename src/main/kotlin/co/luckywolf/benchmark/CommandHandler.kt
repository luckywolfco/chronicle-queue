package co.luckywolf.benchmark

import co.luckywolf.benchmark.Service.Companion.serviceMap
import co.luckywolf.benchmark.Service.UNDEFINED
import net.openhft.chronicle.bytes.BytesIn
import net.openhft.chronicle.bytes.BytesOut
import net.openhft.chronicle.bytes.util.BinaryLengthLength
import net.openhft.chronicle.core.io.IORuntimeException
import net.openhft.chronicle.core.time.SystemTimeProvider
import net.openhft.chronicle.wire.BytesInBinaryMarshallable
import net.openhft.chronicle.wire.LongConversion
import net.openhft.chronicle.wire.NanoTimestampLongConverter
import net.openhft.chronicle.wire.SelfDescribingMarshallable
import net.openhft.chronicle.wire.converter.NanoTime
import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException


object CommandQueueHandler {

    interface PingStatusHandler {
        fun ping(ping: Ping)
    }

    interface PongStatusHandler {
        fun onPong(pong: Pong)
    }

    interface PingBinaryStatusHandler {
        fun ping(ping: PingBinary)
    }

    interface PongBinaryStatusHandler {
        fun onPong(pong: PongBinary)
    }

}

open class Command : SelfDescribingMarshallable() {
    @LongConversion(NanoTimestampLongConverter::class)
//    @NanoTime
    var commandId: Long = 0
    @LongConversion(NanoTimestampLongConverter::class)
//    @NanoTime
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }
}

open class CommandBinary : BytesInBinaryMarshallable() {
//    @NanoTime
//    @LongConversion(NanoTimestampLongConverter::class)
    var commandId: Long = 0
//    @NanoTime
//    @LongConversion(NanoTimestampLongConverter::class)
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }

    @Override
    @Throws(IORuntimeException::class, BufferUnderflowException::class, IllegalStateException::class)
    override fun readMarshallable(bytes: BytesIn<*>) {
        commandId = bytes.readLong()
        traceId = bytes.readLong()
        version = bytes.readInt()
        origin = Service.fromId(bytes.readInt())
    }

    @Throws(
        IllegalStateException::class,
        BufferOverflowException::class,
        BufferUnderflowException::class,
        ArithmeticException::class
    )
    override fun writeMarshallable(bytes: BytesOut<*>) {
        bytes.writeLong(commandId)
        bytes.writeLong(traceId)
        bytes.writeInt(version)
        bytes.writeInt(origin.id)
    }

    override fun binaryLengthLength(): BinaryLengthLength {
        return BinaryLengthLength.LENGTH_16BIT
    }

}

class Ping(var service: Service) : SelfDescribingMarshallable()  {
    @LongConversion(NanoTimestampLongConverter::class)
//    @NanoTime
    var commandId: Long = 0
    @LongConversion(NanoTimestampLongConverter::class)
//    @NanoTime
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }

}//: Command()

class Pong(var service: Service, var pongStatus: PongStatus = PongStatus.UNDEFINED, var comment: String? = null) : SelfDescribingMarshallable() {
    @LongConversion(NanoTimestampLongConverter::class)
//    @NanoTime
    var commandId: Long = 0
    @LongConversion(NanoTimestampLongConverter::class)
//    @NanoTime
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }
}//: Command()

class PingBinary(var service: Service) : BytesInBinaryMarshallable() { //: CommandBinary() {

    var commandId: Long = 0
    //    @NanoTime
//    @LongConversion(NanoTimestampLongConverter::class)
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }

    override fun writeMarshallable(bytes: BytesOut<*>) {
        super.writeMarshallable(bytes)
        bytes.writeInt(service.id)
    }

    override fun readMarshallable(bytes: BytesIn<*>) {
        super.readMarshallable(bytes)
        service = Service.fromId(bytes.readInt())
    }
}
class PongBinary(
    var service: Service,
    var pongStatus: PongStatus = PongStatus.UNDEFINED,
    var comment: String? = null
) : BytesInBinaryMarshallable() { // : CommandBinary() {

    var commandId: Long = 0
    //    @NanoTime
//    @LongConversion(NanoTimestampLongConverter::class)
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }

    override fun writeMarshallable(bytes: BytesOut<*>) {
        super.writeMarshallable(bytes)
        bytes.writeInt(service.id)
        bytes.writeInt(pongStatus.id)
        bytes.write(comment?:"")
    }

    override fun readMarshallable(bytes: BytesIn<*>) {
        super.readMarshallable(bytes)
        service = Service.fromId(bytes.readInt())
        pongStatus = PongStatus.fromId(bytes.readInt())
        comment = bytes.readUtf8()
    }
}


enum class PongStatus(val id: Int) {
    UNDEFINED(0),
    WEBSOCKET_CONNECTED(1),
    WEBSOCKET_DISCONNECTED(2);

    companion object {
        private val statusMap = values().map { it.id to it  }.toMap()
        fun fromId(id: Int): PongStatus {
            val status = statusMap[id]
            return status?: UNDEFINED
        }
    }
}

enum class Service(val id: Int) {
    UNDEFINED(0),
    MARKET_MAKER(1),
    OMS(2),
    VALR_ORDER_DATA(3),
    VALR_SOURCE_MARKET_DATA(4),
    MARKET_DATA(5),
    CONTROL_CENTRE(6),
    GATEWAY(7),
    PORTFOLIO_MANAGER(8),
    LUNO_SOURCE_MARKET_DATA(9),
    BINANCE_SOURCE_MARKET_DATA(10),
    COINBASE_SOURCE_MARKET_DATA(11),
    COINBASE_PRIME_SOURCE_MARKET_DATA(12),
    KRAKEN_SOURCE_MARKET_DATA(13),
    KUCOIN_SOURCE_MARKET_DATA(14),
    BYBIT_ORDER_DATA(15),
    BYBIT_SOURCE_MARKET_DATA(16),
    OKX_SOURCE_MARKET_DATA(17),
    TRADED_24HR_VOLUME(18),
    VALR_FUNDING_RATES(19),
    OMS_ORDER_ROUTER(20),
    ACCOUNT(21);

    companion object {
        private val serviceMap = Service.values().map { it.id to it  }.toMap()
        fun fromId(id: Int): Service {
            val service = serviceMap[id]
            return service?:UNDEFINED
        }
    }
}