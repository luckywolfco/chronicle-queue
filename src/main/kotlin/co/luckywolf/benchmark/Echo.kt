package co.luckywolf.benchmark

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

}