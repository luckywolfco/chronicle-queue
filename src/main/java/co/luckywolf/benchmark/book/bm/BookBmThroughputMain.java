package co.luckywolf.benchmark.book.bm;

import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.queue.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static co.luckywolf.benchmark.book.bm.BookBmJLBH.Task.generateRandomDouble;

public class BookBmThroughputMain {

    static final int time = Integer.getInteger("time", 5);
    static final int size = Integer.getInteger("size", 60);
    static final String path = System.getProperty("path", OS.TMP);
    static final int throughput = Integer.getInteger("throughput", 100_000);
    static final int threads = Integer.getInteger("threads", 1);

    static final boolean fullWrite = Jvm.getBoolean("fullWrite");
    static final int iterations = Integer.getInteger("iterations", 30 * throughput);
    static final BufferMode BUFFER_MODE = getBufferMode();

    static final int WARMUP = 500_000;

    private static BufferMode getBufferMode() {
        String bufferMode = System.getProperty("bufferMode");
        if (bufferMode != null && bufferMode.length() > 0)
            return BufferMode.valueOf(bufferMode);
        BufferMode bm;
        try {
            Class.forName("software.chronicle.enterprise.queue.ChronicleRingBuffer");
            bm = BufferMode.Asynchronous;
        } catch (ClassNotFoundException cnfe) {
            bm = BufferMode.None;
        }
        return bm;
    }

    public static void main(String[] args) {
        System.out.println("Testing with " +
                "-Dtime=" + time + " " +
                "-Dthreads=" + threads + " " +
                "-Dsize=" + size + " " +
                "-Dpath=" + path + " " +
                "-DfullWrite=" + fullWrite);

        String base = path + "/delete-" + Time.uniqueId() + ".me.";

        long blockSize = OS.is64Bit()
                ? OS.isLinux()
                ? 4L << 30
                : 1L << 30
                : 256L << 20;

        final BookBM source = new BookBM();
        source.instrument("EUR/USD");
        for (int i = 0; i < 50; i++) {
            source.addAsk(generateRandomDouble(), generateRandomDouble());
            source.addBid(generateRandomDouble(), generateRandomDouble());
        }

        long start = System.nanoTime();
        AtomicLong count = new AtomicLong();
        IntStream.range(0, threads).parallel().forEach(i -> {
            long count2 = 0;
            try (ChronicleQueue q = ChronicleQueue.singleBuilder(base + i)
                    .rollCycle(RollCycles.FAST_HOURLY)
                    .blockSize(blockSize)
                    .build()) {

                BookEvent bookEventWriter = q.createAppender().methodWriter(BookEvent.class);
                do {
                    source.setTimeStampNs(System.nanoTime());
                    bookEventWriter.process(source);
                    count2++;

                } while (start + time * 1e9 > System.nanoTime());
            }
            count.addAndGet(count2);
        });
        long time1 = System.nanoTime() - start;
        Jvm.pause(1000);
        System.gc();
        long mid = System.nanoTime();
        IntStream.range(0, threads).parallel().forEach(i -> {

            try (ChronicleQueue q = ChronicleQueue.singleBuilder(base + i)
                    .rollCycle(RollCycles.FAST_HOURLY)
                    .blockSize(blockSize)
                    .build()) {
                ExcerptTailer tailer = q.createTailer();
                MethodReader reader = q.createTailer().methodReader(new BookEventReader());
                while(reader.readOne()) {

                }
            }
        });
        long end = System.nanoTime();
        long time2 = end - mid;

        System.out.printf("Writing %,d messages took %.3f seconds, at a rate of %,d per second%n",
                count.longValue(), time1 / 1e9, 1000 * (long) (1e6 * count.get() / time1));
        System.out.printf("Reading %,d messages took %.3f seconds, at a rate of %,d per second%n",
                count.longValue(), time2 / 1e9, 1000 * (long) (1e6 * count.get() / time2));

        Jvm.pause(200);
        System.gc(); // make sure its cleaned up for windows to delete.
        IntStream.range(0, threads).forEach(i ->
                IOTools.deleteDirWithFiles(base + i, 2));
    }
}