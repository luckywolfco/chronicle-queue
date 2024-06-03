package co.luckywolf.benchmark

import net.openhft.chronicle.bytes.BytesStore
import net.openhft.chronicle.core.Jvm
import net.openhft.chronicle.core.OS
import net.openhft.chronicle.core.UnsafeMemory
import net.openhft.chronicle.core.io.IOTools
import net.openhft.chronicle.core.util.Time
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.wire.Wire
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream

/* Ubuntu i7-10710U CPU @ 1.10GHz
Writing 23,068,441 messages took 5.076 seconds, at a rate of 4,544,000 per second
Reading 23,068,441 messages took 2.728 seconds, at a rate of 8,456,000 per second
*/
object ThroughputMain {
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