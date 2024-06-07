package co.luckywolf.benchmark

import co.luckywolf.benchmark.md.*
import net.openhft.chronicle.core.util.Histogram

object Echo {
    val pong = Pong(Service.GATEWAY)
    class ReadPing() : CommandQueueHandler.PingStatusHandler {
        override fun ping(ping: Ping) {
            pong.traceId = ping.traceId
            pong.origin = ping.origin
            pong.service = ping.service
            pong.commandId = System.nanoTime()
        }
    }

    class ReadPingWritePong(val pongStatusHandler: CommandQueueHandler.PongStatusHandler) : CommandQueueHandler.PingStatusHandler {
        override fun ping(ping: Ping) {
            pong.traceId = ping.traceId
            pong.origin = ping.origin
            pong.service = ping.service
            pong.commandId = System.nanoTime()
            pongStatusHandler.onPong(pong)
        }
    }

    class ReadPong(val histogramCo: Histogram,
                   val histogramIn: Histogram,
    ) : CommandQueueHandler.PongStatusHandler {

        override fun onPong(pong: Pong) {
            val startCo = pong.traceId //bytes.readLong() // when it should have started
            val startIn = pong.commandId //bytes.readLong() // when it actually started
            val now = System.nanoTime()
            histogramCo.sample((now - startCo).toDouble())
            histogramIn.sample((now - startIn).toDouble())
        }
    }

    val pongBinary = PongBinary(Service.GATEWAY)
    class ReadPingBinary() : CommandQueueHandler.PingBinaryStatusHandler {
        override fun ping(ping: PingBinary) {
            pongBinary.traceId = ping.traceId
            pongBinary.origin = ping.origin
            pongBinary.service = ping.service
            pongBinary.commandId = System.nanoTime()
        }
    }

    class ReadPingWritePongBinary(val pongStatusHandler: CommandQueueHandler.PongBinaryStatusHandler) : CommandQueueHandler.PingBinaryStatusHandler {
        override fun ping(ping: PingBinary) {
            pongBinary.traceId = ping.traceId
            pongBinary.origin = ping.origin
            pongBinary.service = ping.service
            pongBinary.commandId = System.nanoTime()
            pongStatusHandler.onPong(pongBinary)
        }
    }

    class ReadPongBinary(val histogramCo: Histogram,
                   val histogramIn: Histogram,
    ) : CommandQueueHandler.PongBinaryStatusHandler {

        override fun onPong(pong: PongBinary) {
            val startCo = pong.traceId //bytes.readLong() // when it should have started
            val startIn = pong.commandId //bytes.readLong() // when it actually started
            val now = System.nanoTime()
            histogramCo.sample((now - startCo).toDouble())
            histogramIn.sample((now - startIn).toDouble())
        }
    }

    class ReadMarketData() : CommandQueueHandler.MarketDataHandler {
        override fun onMarketData(marketData: MarketDepth) {
            pong.traceId = marketData.traceId
            pong.origin = marketData.service
            pong.service = marketData.service
            pong.commandId = System.nanoTime()
        }
    }

    class ReadMarketDataSet() : CommandQueueHandler.MarketDataSetHandler {
        override fun onMarketData(marketData: MarketDepthSet) {
            pong.traceId = marketData.traceId
            pong.origin = marketData.service
            pong.service = marketData.service
            pong.commandId = System.nanoTime()
        }
    }

    class ReadArrayMarketData() : CommandQueueHandler.MarketDataArrayHandler {
        override fun onMarketData(marketData: MarketDepthArray) {
            pong.traceId = marketData.traceId
            pong.origin = marketData.service
            pong.service = marketData.service
            pong.commandId = System.nanoTime()
        }
    }

    class ReadBinaryMarketData() : CommandQueueHandler.MarketDataBinaryHandler {
        override fun onMarketData(marketData: MarketDepthBinary) {
            pong.traceId = marketData.traceId
            pong.origin = marketData.service
            pong.service = marketData.service
            pong.commandId = System.nanoTime()
        }
    }

    class ReadBinary2MarketData() : CommandQueueHandler.MarketDataBinary2Handler {
        override fun onMarketData(marketData: MarketDepthBinary2) {
            pong.traceId = marketData.traceId
            pong.origin = marketData.service
            pong.service = marketData.service
            pong.commandId = System.nanoTime()
        }
    }

    class ReadItem() : CommandQueueHandler.MarketDataItemHandler {
        override fun onItem(depthItem: Item) {
            pong.traceId = depthItem.timestampNs
            pong.commandId = System.nanoTime()
        }
    }



}