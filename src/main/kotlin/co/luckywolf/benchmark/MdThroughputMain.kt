package co.luckywolf.benchmark

import net.openhft.chronicle.core.Jvm
import net.openhft.chronicle.core.OS
import net.openhft.chronicle.core.io.IOTools
import net.openhft.chronicle.core.util.Time
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.RollCycles
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream


object MdThroughputMain {

    class BuyComparator(): Comparator<DepthItem> {
        override fun compare(o1: DepthItem, o2: DepthItem): Int {
            return -1 * o1.priceBigDecimal().compareTo(o2.priceBigDecimal())
        }
    }

    class SellComparator(): Comparator<DepthItem> {
        override fun compare(o1: DepthItem, o2: DepthItem): Int {
            return o1.priceBigDecimal().compareTo(o2.priceBigDecimal())
        }
    }

    var expectedAsks = sortedSetOf(
        SellComparator(),
        DepthItem(
            26697.55.toBigDecimal(),
            0.042655.toBigDecimal()
        ),
        DepthItem(
            26852.51.toBigDecimal(),
            0.314944.toBigDecimal()
        ),
        DepthItem(
            26993.34.toBigDecimal(),
            0.743122.toBigDecimal()
        ),
        DepthItem(
            27072.23.toBigDecimal(),
            0.005099.toBigDecimal()
        ),
        DepthItem(
            27134.17.toBigDecimal(),
            0.000519.toBigDecimal()
        ),
        DepthItem(
            27275.toBigDecimal(),
            0.000519.toBigDecimal()
        ),
        DepthItem(
            27415.83.toBigDecimal(),
            0.000519.toBigDecimal()
        ),
        DepthItem(
            27470.35.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            27556.66.toBigDecimal(),
            0.000519.toBigDecimal()
        ),
        DepthItem(
            27697.49.toBigDecimal(),
            0.000519.toBigDecimal()
        ),
        DepthItem(
            27838.32.toBigDecimal(),
            0.000519.toBigDecimal()
        ),
        DepthItem(
            27868.47.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            27999.98.toBigDecimal(),
            0.000845.toBigDecimal()
        ),
        DepthItem(
            28266.59.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            28664.71.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            29062.83.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            29175.71.toBigDecimal(),
            0.037505.toBigDecimal()
        ),
        DepthItem(
            29184.1.toBigDecimal(),
            0.020297.toBigDecimal()
        ),
        DepthItem(
            29187.65.toBigDecimal(),
            0.039265.toBigDecimal()
        ),
        DepthItem(
            29228.83.toBigDecimal(),
            0.060962.toBigDecimal()
        ),
        DepthItem(
            29237.73.toBigDecimal(),
            0.067681.toBigDecimal()
        ),
        DepthItem(
            29460.95.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            29859.07.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            30257.19.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            30655.31.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            31053.43.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            31451.55.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            31849.67.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            32247.79.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            32645.91.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            33044.03.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            33442.15.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            33840.27.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            34238.39.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            34636.51.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            34989.toBigDecimal(),
            13.840654.toBigDecimal()
        ),
        DepthItem(
            35034.63.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            35432.75.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            35830.87.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            36228.99.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            36627.11.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            37025.23.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            37423.35.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            37821.47.toBigDecimal(),
            0.006329.toBigDecimal()
        ),
        DepthItem(
            38100.toBigDecimal(),
            0.032849.toBigDecimal()
        ),
        DepthItem(
            38219.59.toBigDecimal(),
            0.006543.toBigDecimal()
        ),
        DepthItem(
            38617.71.toBigDecimal(),
            0.006543.toBigDecimal()
        ),
        DepthItem(
            39015.83.toBigDecimal(),
            0.006543.toBigDecimal()
        ),
        DepthItem(
            39413.95.toBigDecimal(),
            0.006543.toBigDecimal()
        ),
        DepthItem(
            39812.07.toBigDecimal(),
            0.006543.toBigDecimal()
        )
    )

    var expectedBids = sortedSetOf( BuyComparator(),
        DepthItem(
            26697.54.toBigDecimal(),
            0.045605.toBigDecimal()
        ),
        DepthItem(
            26689.15.toBigDecimal(),
            0.050903.toBigDecimal()
        ),
        DepthItem(
            26687.79.toBigDecimal(),
            0.000607.toBigDecimal()
        ),
        DepthItem(
            26687.68.toBigDecimal(),
            0.003685.toBigDecimal()
        ),
        DepthItem(
            26679.59.toBigDecimal(),
            0.00939.toBigDecimal()
        ),
        DepthItem(
            26674.2.toBigDecimal(),
            0.005472.toBigDecimal()
        ),
        DepthItem(
            26673.5.toBigDecimal(),
            0.02059.toBigDecimal()
        ),
        DepthItem(
            26570.85.toBigDecimal(),
            0.011544.toBigDecimal()
        ),
        DepthItem(
            26430.02.toBigDecimal(),
            0.013114.toBigDecimal()
        ),
        DepthItem(
            26289.19.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            26275.99.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            26148.36.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            26007.53.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            26007.52.toBigDecimal(),
            0.004434.toBigDecimal()
        ),
        DepthItem(
            25877.87.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            25866.7.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            25725.87.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            25585.04.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            25479.75.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            25444.21.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            25303.38.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            25162.55.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            25081.63.toBigDecimal(),
            0.003052.toBigDecimal()
        ),
        DepthItem(
            25021.72.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            24880.89.toBigDecimal(),
            0.00052.toBigDecimal()
        ),
        DepthItem(
            24683.51.toBigDecimal(),
            0.006076.toBigDecimal()
        ),
        DepthItem(
            24678.24.toBigDecimal(),
            0.000846.toBigDecimal()
        ),
        DepthItem(
            24285.39.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            23887.27.toBigDecimal(),
            0.003171.toBigDecimal()
        ),
        DepthItem(
            23489.15.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            23091.03.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            23017.37.toBigDecimal(),
            0.000846.toBigDecimal()
        ),
        DepthItem(
            22692.91.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            22294.79.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            21896.67.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            21498.55.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            21356.5.toBigDecimal(),
            0.000846.toBigDecimal()
        ),
        DepthItem(
            21100.43.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            20702.31.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            20304.19.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            20000.toBigDecimal(),
            0.01.toBigDecimal()
        ),
        DepthItem(
            19906.07.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            19695.63.toBigDecimal(),
            0.000846.toBigDecimal()
        ),
        DepthItem(
            19507.95.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            19109.83.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            18711.71.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            18313.59.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            18034.76.toBigDecimal(),
            0.000846.toBigDecimal()
        ),
        DepthItem(
            17915.47.toBigDecimal(),
            0.006336.toBigDecimal()
        ),
        DepthItem(
            17517.35.toBigDecimal(),
            0.006336.toBigDecimal()
        )
    )

    val md = MarketDepth()

    fun md() {
        md.instrument = Instrument.VALR_BTC_ZAR
//        asks.
        md.asks
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(
            "Testing with " +
                    "-Dtime=" + EchoBenchmarkMain.time + " " +
                    "-Dthreads=" + EchoBenchmarkMain.threads + " " +
                    "-Dsize=" + EchoBenchmarkMain.size + " " +
                    "-Dpath=" + EchoBenchmarkMain.path + " " +
                    "-DfullWrite=" + EchoBenchmarkMain.fullWrite
        )

        val start = System.nanoTime()
        val base = EchoBenchmarkMain.path + "/delete-" + Time.uniqueId() + ".me."

        val blockSize = if (OS.is64Bit()
        ) if (OS.isLinux()
        ) 4L shl 30
        else 1L shl 30
        else 256L shl 20

        val count = AtomicLong()
        IntStream.range(0, EchoBenchmarkMain.threads).parallel().forEach { i: Int ->
            var count2: Long = 0

            ChronicleQueue.singleBuilder(base + i)
                .rollCycle(RollCycles.FAST_HOURLY)
                .blockSize(blockSize)
                .build().use { q ->
                    val appender = q.acquireAppender()
                    val writer = appender.methodWriter(CommandQueueHandler.PingStatusHandler::class.java)
                    var lastIndex: Long = -1
                    val ping = Ping(Service.GATEWAY)
                    do {
                        ping.traceId = System.nanoTime()
                        ping.commandId = ping.traceId
                        writer.ping(ping)
                        count2++
                    } while (start + EchoBenchmarkMain.time * 1e9 > System.nanoTime())
                }
            count.addAndGet(count2)
        }
        val time1 = System.nanoTime() - start
        Jvm.pause(1000)
        System.gc()
        val mid = System.nanoTime()
        IntStream.range(0, EchoBenchmarkMain.threads).parallel().forEach { i: Int ->
            ChronicleQueue.singleBuilder(base + i)
                .rollCycle(RollCycles.FAST_HOURLY)
                .blockSize(blockSize)
                .build().use { q ->
                    val tailer = q.createTailer()
                    val pingReader = tailer.methodReader(Echo.ReadPing())
                    while (pingReader.readOne()) {}
                }
        }
        val end = System.nanoTime()
        val time2 = end - mid

        System.out.printf(
            "Writing %,d messages took %.3f seconds, at a rate of %,d per second%n",
            count.toLong(), time1 / 1e9, 1000 * (1e6 * count.get() / time1).toLong()
        )
        System.out.printf(
            "Reading %,d messages took %.3f seconds, at a rate of %,d per second%n",
            count.toLong(), time2 / 1e9, 1000 * (1e6 * count.get() / time2).toLong()
        )

        Jvm.pause(200)
        System.gc() // make sure its cleaned up for windows to delete.
        IntStream.range(0, EchoBenchmarkMain.threads).forEach { i: Int ->
            IOTools.deleteDirWithFiles(base + i, 2)
        }
    }
}