package co.luckywolf.benchmark

import net.openhft.affinity.Affinity
import net.openhft.affinity.AffinityLock
import net.openhft.chronicle.bytes.BytesStore
import net.openhft.chronicle.core.Jvm
import net.openhft.chronicle.core.OS
import net.openhft.chronicle.core.io.IOTools
import net.openhft.chronicle.core.util.Histogram
import net.openhft.chronicle.core.util.Time
import net.openhft.chronicle.queue.BufferMode
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.ExcerptAppender
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import java.io.File

/**
 * Results 27/10/2015 running on a MBP
 * 50/90 99/99.9 99.99/99.999 - worst was 1.5 / 27  104 / 3,740  8,000 / 13,890 - 36,700
 *
 *
 * Results 14/03/2016 running on E5-2650v2
 * 50/90 99/99.9 99.99 - worst was 0.88 / 1.4  10.0 / 19  72 - 483
 *
 *
 * Results 23/03/2016 running on E5-2643 Debian Kernel 4.2
 * 50/90 99/99.9 99.99 - worst was 0.56 / 0.82  5.0 / 12  40 - 258
 *
 *
 * Results 23/03/2016 running on Linux VM (i7-4800MQ) Debian Kernel 4.2
 * 50/90 99/99.9 99.99 - worst was 0.50 / 1.6  21 / 84  573 - 1,410
 *
 *
 * Results 23/03/2016 running on E3-1505Mv5 Debian Kernel 4.5
 * 50/90 99/99.9 99.99 - worst was 0.33 / 0.36  1.6 / 3.0  18 - 160
 *
 *
 * Results 03/02/2017 running on i7-6700HQ Win 10  100k/s * 5M * 40B
 * 50/90 99/99.9 99.99/99.999 - worst was 0.59 / 0.94  17 / 135  12,850 / 15,470 - 15,990
 *
 *
 * Results 03/02/2017 running on i7-6700HQ Win 10  100k/s * 5M * 40B
 * 50/90 99/99.9 99.99/99.999 - worst was 0.39 / 0.39  0.39 / 28  541 / 967  1,280 / 3,340
 *
 *
 * Results 06/02/2017 running on i7-6700HQ Win 10  1M/s * 5M * 40B
 * 50/90 99/99.9 99.99/99.999 - worst was 0.39 / 0.39  6.3 / 76  516 / 868  999 / 1,030
 *
 *
 * Results 06/02/2017 running on i7-6700HQ Win 10  1.2M/s * 5M * 40B
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.39 / 0.39  12 / 336  2,820 / 3,470  3,600 / 3,600
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 100k/s * 5 M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 - worst was 0.18 / 0.20  0.26 / 0.59  10 / 14 - 117
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 100k/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.19 / 0.23  0.31 / 0.72  10 / 15  88 / 176
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 500k/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.19 / 0.20  0.24 / 8.4  12 / 60  160 / 176
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 500k/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.19 / 0.21  0.25 / 9.0  11 / 76  125 / 135
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 1M/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.19 / 0.20  0.33 / 9.0  15 / 143  176 / 176
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 1.4M/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.18 / 0.20  3.6 / 9.5  96 / 303  336 / 336
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 2.0M/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.19 / 0.20  5.5 / 12  639 / 901  934 / 934
 *
 *
 * Results 05/02/2017 running i7-4790, Centos 7 2.3M/s * 20M * 40B enableAffinity=true
 * 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.19 / 0.21  9.5 / 6,160  9,700 / 9,700  9,700 / 9,700
 *
 *
 * Results 27/10/2017 running i7-4790, Centos 7 100K/s * 20 M * 40B enableAffinity=true
 * wr: 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.014 / 0.017  0.017 / 0.021  0.026 / 0.91  20 / 104
 * in: 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.20 / 0.23  0.25 / 1.2  1.5 / 10  29 / 143
 * co: 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.21 / 0.24  0.26 / 1.2  1.5 / 10  56 / 143
 *
 *
 * Results 27/10/2017 running i7-4790, Centos 7 1M/s * 20 M * 40B enableAffinity=true
 * wr: 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.014 / 0.017  0.019 / 0.025  8.1 / 58  96 / 104
 * in: 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.18 / 0.20  0.24 / 0.82  6.8 / 88  143 / 143
 * co: 50/90 99/99.9 99.99/99.999 99.9999/worst was 0.20 / 0.21  0.25 / 0.94  13 / 100  143 / 143
 *
 *
 * I ran with
 * mvn -DenableAffinity=true exec:java -Dexec.classpathScope="test" -Dexec.mainClass=net.openhft.chronicle.queue.benchmark.LatencyDistributionMain
 *
 *
 * Run with 5.19.42, on tmpfs
 * wr: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.016 / 0.016  0.017 / 0.017  0.017 / 0.017  0.35 / 20  68 / 186  511 / 584 - 612
 * in: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.23 / 0.24  0.25 / 0.28  0.30 / 0.33  0.73 / 1.5  44 / 137  1,490 / 2,070 - 2,330
 * co: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.24 / 0.26  0.26 / 0.30  0.32 / 0.35  1.4 / 39  92 / 493  1,490 / 2,070 - 2,350
 * Run with 5.19.42 to an NVMe drive
 * wr: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.016 / 0.016  0.016 / 0.017  0.017 / 0.017  0.69 / 30  119 / 764  1,590 / 2,080 - 2,320
 * in: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.20 / 0.22  0.22 / 0.27  0.28 / 0.49  1.0 / 1.8  41 / 69  98 / 164 - 2,300
 * co: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.22 / 0.23  0.24 / 0.28  0.30 / 0.93  1.7 / 46  135 / 764  1,590 / 2,150 - 2,430
 */
class BinaryLatencyDistributionMain {
    @Throws(InterruptedException::class)
    fun run(args: Array<String>?) {
        val blockSize = if (OS.is64Bit()
        ) if (OS.isLinux()
        ) 4L shl 30
        else 1L shl 30
        else 256L shl 20
        val tmpDir = tmpDir
        val builder = SingleChronicleQueueBuilder
            .binary(tmpDir)
            .rollCycle(RollCycles.FAST_HOURLY)
//            .blockSize(128 shl 20)
            .blockSize(blockSize)
        builder
            .writeBufferMode(EchoBenchmarkMain.BUFFER_MODE)
            .readBufferMode(BufferMode.None)
            .build().use { queue ->
                builder
                    .writeBufferMode(BufferMode.None)
                    .readBufferMode(EchoBenchmarkMain.BUFFER_MODE)
                    .build().use { queue2 ->
                        runTest(queue, queue2)
                    }
            }
        IOTools.deleteDirWithFiles(tmpDir, 2)
    }

    private val tmpDir: File
        get() = File(EchoBenchmarkMain.path + "/delete-" + Time.uniqueId() + ".me")

    @Throws(InterruptedException::class)
    protected fun runTest(queue: ChronicleQueue, queue2: ChronicleQueue) {
        val histogramCo = Histogram()
        val histogramIn = Histogram()
        val histogramWr = Histogram()

        val name = javaClass.name
        val pongTailerThread = Thread {
            var lock: AffinityLock? = null
            try {
                if (Jvm.getBoolean("enableTailerAffinity") || !Jvm.getBoolean("disableAffinity")) {
                    lock = Affinity.acquireLock()
                }
                val pongTailer = queue.createTailer()
                val pongReader = pongTailer.methodReader(Echo.ReadPongBinary(histogramCo, histogramIn))

                var counter = 0
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        if(pongReader.readOne()) {
                            val count = counter++
                            if (count == EchoBenchmarkMain.WARMUP) {
                                    histogramCo.reset()
                                    histogramIn.reset()
                                    histogramWr.reset()
                            }

                            if (count % INTLOG_INTERVAL == 0) println("read  $count")
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
                println("read pong count processed $counter")
            } finally {
                lock?.release()
            }
        }

        val pingTailerThread = Thread {
            var lock: AffinityLock? = null
            try {
                if (Jvm.getBoolean("enableTailerAffinity") || !Jvm.getBoolean("disableAffinity")) {
                    lock = Affinity.acquireLock()
                }
                val tailer = queue2.createTailer()
                val pongWriter = queue2.acquireAppender().methodWriter(CommandQueueHandler.PongBinaryStatusHandler::class.java)
                val pingReader = tailer.methodReader(Echo.ReadPingWritePongBinary(pongWriter))

                var counter = 0
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        if(pingReader.readOne()) {
                            val count = counter++
                            if (count % INTLOG_INTERVAL == 0) println("read write $count")
                        }

                    } catch (e: Exception) {
                        break
                    }
                }

                println("read ping write pong count processed $counter")

            } finally {
                lock?.release()
            }
        }

        val pingAppenderThread = Thread {
            var lock: AffinityLock? = null
            try {
                if (Jvm.getBoolean("enableAppenderAffinity") || !Jvm.getBoolean("disableAffinity")) {
                    lock = Affinity.acquireLock()
                }

                val pingAppender: ExcerptAppender = queue.createAppender()
                val pingWriter = pingAppender.methodWriter(CommandQueueHandler.PingBinaryStatusHandler::class.java)

                var next = System.nanoTime()
                val interval = (1000000000 / EchoBenchmarkMain.throughput).toLong()
                val stackCount: Map<String, Int> = LinkedHashMap()
                val ping = PingBinary(Service.GATEWAY)
                for (i in -EchoBenchmarkMain.WARMUP until EchoBenchmarkMain.iterations) {
                    val s0 = System.nanoTime()
                    if (s0 < next) {
                        do while (System.nanoTime() < next)
                        next = System.nanoTime() // if we failed to come out of the spin loop on time, reset next.
                    }

                    val start = System.nanoTime()
//                    pingAppender.writingDocument(false).use { dc ->
//                        val wire = dc.wire()
//                        val bytes2 = wire!!.bytes()
//                        bytes2.writeLong(next) // when it should have started
//                        bytes2.writeLong(start) // when it actually started.
//                        bytes2.write(bytes24)
//                        ThroughputMain.addToEndOfCache(wire)
//                    }
                    ping.traceId = next
                    ping.commandId = start
                    pingWriter.ping(ping)
                    val time = System.nanoTime() - start
                    histogramWr.sample((start - next).toDouble())
                    next += interval
                    if (i % INTLOG_INTERVAL == 0) println("wrote $i")
                }
                stackCount.entries.stream()
                    .filter { e: Map.Entry<String, Int> -> e.value > 1 }
                    .forEach { x: Map.Entry<String, Int>? -> println(x) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (lock != null) {
                    lock!!.release()
                }
            }
        }
        pingTailerThread.start()
        pongTailerThread.start()

        pingAppenderThread.start()
        pingAppenderThread.join()

//        pretoucher.interrupt()
//        pretoucher.join()

        //Pause to allow tailer to catch up (if needed)
        Jvm.pause(500)
        pingTailerThread.interrupt()
        pingTailerThread.join()
        pongTailerThread.interrupt()
        pongTailerThread.join()

        println("wr: " + histogramWr.toLongMicrosFormat())
        println("in: " + histogramIn.toLongMicrosFormat())
        println("co: " + histogramCo.toLongMicrosFormat())
    }

    companion object {
        private const val INTLOG_INTERVAL = 20000000

        @Throws(InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            assert(false) { "test runs slower with assertions on" }

            println(
                "Testing with " +
                        "-Dtime=" + EchoBenchmarkMain.time + " " +
                        "-Dthreads=" + EchoBenchmarkMain.threads + " " +
                        "-Dsize=" + EchoBenchmarkMain.size + " " +
                        "-Dpath=" + EchoBenchmarkMain.path + " " +
                        "-Dthroughput=" + EchoBenchmarkMain.throughput + " " +
                        "-Dinterations=" + EchoBenchmarkMain.iterations
            )
            BinaryLatencyDistributionMain().run(args)
        }
    }
}