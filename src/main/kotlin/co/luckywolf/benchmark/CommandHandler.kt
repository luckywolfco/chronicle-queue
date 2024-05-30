package co.luckywolf.benchmark

import net.openhft.chronicle.core.time.SystemTimeProvider
import net.openhft.chronicle.wire.LongConversion
import net.openhft.chronicle.wire.NanoTimestampLongConverter
import net.openhft.chronicle.wire.SelfDescribingMarshallable

object CommandQueueHandler {

    interface PingStatusHandler {
        fun ping(ping: Ping)
    }

    interface PongStatusHandler {
        fun onPong(pong: Pong)
    }
}

open class Command : SelfDescribingMarshallable() {
    @LongConversion(NanoTimestampLongConverter::class)
    var commandId: Long = 0
    @LongConversion(NanoTimestampLongConverter::class)
    var traceId: Long = 0
    var version: Int = 1
    var origin = Service.UNDEFINED

    init {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos()
        traceId = commandId
    }
}

class Ping(var service: Service) : Command()
class Pong(var service: Service, var pongStatus: PongStatus = PongStatus.UNDEFINED, var comment: String? = null) : Command()

enum class PongStatus {
    UNDEFINED,
    WEBSOCKET_CONNECTED,
    WEBSOCKET_DISCONNECTED;
}

enum class Service {
    UNDEFINED,
    MARKET_MAKER,
    OMS,
    VALR_ORDER_DATA,
    VALR_SOURCE_MARKET_DATA,
    MARKET_DATA,
    CONTROL_CENTRE,
    GATEWAY,
    PORTFOLIO_MANAGER,
    LUNO_SOURCE_MARKET_DATA,
    BINANCE_SOURCE_MARKET_DATA,
    COINBASE_SOURCE_MARKET_DATA,
    COINBASE_PRIME_SOURCE_MARKET_DATA,
    KRAKEN_SOURCE_MARKET_DATA,
    KUCOIN_SOURCE_MARKET_DATA,
    BYBIT_ORDER_DATA,
    BYBIT_SOURCE_MARKET_DATA,
    OKX_SOURCE_MARKET_DATA,
    TRADED_24HR_VOLUME,
    VALR_FUNDING_RATES,
    OMS_ORDER_ROUTER,
    ACCOUNT;
}