package co.luckywolf.benchmark;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.BufferMode;

public class JEchoBenchmarkMain {
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

    public static void main(String[] args) throws InterruptedException {
        JThroughputMain.main(args);
        JLatencyDistributionMain.main(args);
    }
}
