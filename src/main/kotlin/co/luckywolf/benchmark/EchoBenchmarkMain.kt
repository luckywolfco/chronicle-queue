package co.luckywolf.benchmark

import net.openhft.chronicle.core.Jvm
import net.openhft.chronicle.core.OS
import net.openhft.chronicle.queue.BufferMode


/**
 * Centos 7.5 i7-7820X, using SSD.
 * Testing with -Dtime=20 -Dsize=60 -Dpath=/var/tmp -Dthroughput=1000000 -Dinterations=300000000
 * Writing 396,290,475 messages took 20.314 seconds, at a rate of 19,508,000 per second
 * Reading 396,290,475 messages took 19.961 seconds, at a rate of 19,853,000 per second
 * in: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst
 * was 0.20 / 0.22  0.23 / 0.24  1.4 / 1.6  2.2 / 2.5  3.4 / 7.6  46 / 76 - 7,730
 *
 *
 * Centos 7.6, Gold 6254 tmpfs
 * Writing 530,451,691 messages took 20.030 seconds, at a rate of 26,483,000 per second
 * Reading 530,451,691 messages took 32.158 seconds, at a rate of 16,495,000 per second
 * in: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst
 * was 0.22 / 0.28  0.30 / 0.34  0.94 / 1.1  2.0 / 3.3  16 / 40  225 / 336 - 3,210 (same socket)
 * was 0.30 / 0.34  0.36 / 0.38  0.94 / 1.1  2.6 / 3.5  16 / 48  270 / 369 - 3,210 (diff socket)
 *
 *
 * Centos 7.6, Gold 6254 SSD
 *
 *
 * in: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst
 * was 0.21 / 0.28  0.33 / 0.36  1.2 / 1.8  3.4 / 5.5  17 / 168  451 / 573 - 59,770
 * in: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst
 * was 0.34 / 0.42  0.44 / 0.78  2.1 / 5.2  7.0 / 9.5  12 / 68  176 / 500 - 34,600
 */
class EchoBenchmarkMain {

    companion object {

        val time: Int = Integer.getInteger("time", 5)

        val size: Int = Integer.getInteger("size", 60)

        val path: String = System.getProperty("path", OS.TMP)

        val throughput: Int = Integer.getInteger("throughput", 100000)

        val threads: Int = Integer.getInteger("threads", 1)

        val fullWrite: Boolean = Jvm.getBoolean("fullWrite")

        val iterations: Int = Integer.getInteger("iterations", 30 * throughput)

        private val bufferMode: BufferMode
            get() {
                val bufferMode = System.getProperty("bufferMode")
                if (bufferMode != null && bufferMode.length > 0) return BufferMode.valueOf(bufferMode)
                var bm: BufferMode
                try {
                    Class.forName("software.chronicle.enterprise.queue.ChronicleRingBuffer")
                    bm = BufferMode.Asynchronous
                } catch (cnfe: ClassNotFoundException) {
                    bm = BufferMode.None
                }
                return bm
            }

        val BUFFER_MODE: BufferMode = bufferMode


        const val WARMUP: Int = 500000

        @Throws(InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            ThroughputMain.main(args)
            LatencyDistributionMain.main(args)
        }
    }
}