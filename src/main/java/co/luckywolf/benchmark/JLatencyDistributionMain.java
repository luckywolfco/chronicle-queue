package co.luckywolf.benchmark;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.util.Histogram;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.queue.*;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class JLatencyDistributionMain {
    private static final int INTLOG_INTERVAL = 20_000_000;
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
        System.out.println(
                "Testing with " +
                        "-Dtime=" + JEchoBenchmarkMain.time + " " +
                        "-Dthreads=" + JEchoBenchmarkMain.threads + " " +
                        "-Dsize=" + JEchoBenchmarkMain.size + " " +
                        "-Dpath=" + JEchoBenchmarkMain.path + " " +
                        "-Dthroughput=" + JEchoBenchmarkMain.throughput + " " +
                        "-Dinterations=" + JEchoBenchmarkMain.iterations
        );
        new JLatencyDistributionMain().run(args);
    }

    private void run(String[] args) {
        File tmpDir = tmpDir();

        SingleChronicleQueueBuilder builder = SingleChronicleQueueBuilder
                .binary(tmpDir)
                .rollCycle(RollCycles.FAST_HOURLY).
                blockSize(128 << 20);
        try (ChronicleQueue queue = builder
                .writeBufferMode(BUFFER_MODE)
                .readBufferMode(BufferMode.None)
                .build();
             ChronicleQueue queue2 = builder
                     .writeBufferMode(BufferMode.None)
                     .readBufferMode(BUFFER_MODE)
                     .build()) {

            runTest(queue, queue2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        IOTools.deleteDirWithFiles(tmpDir, 2);
    }

    private void runTest(ChronicleQueue queue, ChronicleQueue queue2) throws InterruptedException {
        Histogram histogramCo = new Histogram();
        Histogram histogramIn = new Histogram();
        Histogram histogramWr = new Histogram();

        Thread pongTailerThread = new Thread(() -> {
            AffinityLock lock = null;
            try {
                if (Jvm.getBoolean("enableTailerAffinity") || !Jvm.getBoolean("disableAffinity")) {
                    lock = Affinity.acquireLock();
                }
                ExcerptTailer pongTailer = queue.createTailer();
                MethodReader pongReader = pongTailer.methodReader(new JReadPong(histogramCo, histogramIn));

                int counter = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if(pongReader.readOne()) {
                            long count = counter++;
                            if (count == EchoBenchmarkMain.WARMUP) {
                                histogramCo.reset();
                                histogramIn.reset();
                                histogramWr.reset();
                            }

                            if (count % INTLOG_INTERVAL == 0) System.out.println("read  "+count);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                if (lock != null) {
                    lock.release();
                }
            }
        });

        Thread pingTailerThread = new Thread(() -> {
            AffinityLock lock = null;
            try {
                if (Jvm.getBoolean("enableTailerAffinity") || !Jvm.getBoolean("disableAffinity")) {
                    lock = Affinity.acquireLock();
                }
                ExcerptTailer tailer = queue2.createTailer();
                JCommandQueueHandler.PongStatusHandler pongStatusHandler = queue2.createAppender().methodWriter(JCommandQueueHandler.PongStatusHandler.class);
                MethodReader pingReader = tailer.methodReader(new JReadPingWritePong(pongStatusHandler));

                long counter = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if(pingReader.readOne()) {
                            long count = counter++;
                            if (count % INTLOG_INTERVAL == 0) System.out.println("read write "+count);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("read ping write pong count processed $counter");

            } finally {
                if (lock != null) {
                    lock.release();
                }
            }
        });

        Thread appenderThread = new Thread(() -> {
            AffinityLock lock = null;
            try {
                if (Jvm.getBoolean("enableAppenderAffinity") || !Jvm.getBoolean("disableAffinity")) {
                    lock = Affinity.acquireLock();
                }
                JPing ping = new JPing(Service.GATEWAY);
                ExcerptAppender pingAppender = queue.createAppender();
                JCommandQueueHandler.PongStatusHandler pongWriter = queue2.createAppender().methodWriter(JCommandQueueHandler.PongStatusHandler.class);
                JCommandQueueHandler.PingStatusHandler pingWriter = pingAppender.methodWriter(JCommandQueueHandler.PingStatusHandler.class);
                long next = System.nanoTime();
                long interval = 1_000_000_000 / throughput;
                Map<String, Integer> stackCount = new LinkedHashMap<>();
                for (int i = -WARMUP; i < iterations; i++) {
                    long s0 = System.nanoTime();
                    if (s0 < next) {
                        do ; while (System.nanoTime() < next);
                        next = System.nanoTime(); // if we failed to come out of the spin loop on time, reset next.
                    }

                    long start = System.nanoTime();
                    ping.traceId = next;
                    ping.commandId = start;
//                    long time = System.nanoTime() - start;
                    pingWriter.ping(ping);
                    histogramWr.sample(start - next);

                    next += interval;
                    if (i % INTLOG_INTERVAL == 0) System.out.println("wrote " + i);
                }
                stackCount.entrySet().stream()
                        .filter(e -> e.getValue() > 1)
                        .forEach(System.out::println);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (lock != null) {
                    lock.release();
                }
            }
        });

        pongTailerThread.start();
        pingTailerThread.start();

        appenderThread.start();
        appenderThread.join();

        //Pause to allow tailer to catch up (if needed)
        Jvm.pause(500);
        pingTailerThread.interrupt();
        pingTailerThread.join();
        pongTailerThread.interrupt();
        pongTailerThread.join();

        System.out.println("wr: " + histogramWr.toLongMicrosFormat());
        System.out.println("in: " + histogramIn.toLongMicrosFormat());
        System.out.println("co: " + histogramCo.toLongMicrosFormat());

    }

    private File tmpDir() {
        return new File(JEchoBenchmarkMain.path + "/delete-" + Time.uniqueId() + ".me");
    }
}
