package co.luckywolf.benchmark;

import co.luckywolf.benchmark.md.binarymarshable.JBinaryBigDecimalDepthItem;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;
import net.openhft.chronicle.wire.BinaryWire;

import java.math.BigDecimal;

public class EventLoopJLBH {

    static final int ITERATIONS = Integer.getInteger("iterations", 100_000);
    static final int THROUGHPUT = Integer.getInteger("throughput", 100_000);
    static final String PATH = Jvm.getProperty("path", OS.getTarget() + "/data");

    /**
     * Main method to start the JLBH benchmark.
     * Configures JLBHOptions with predefined settings and initiates the benchmark.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        IOTools.deleteDirWithFiles(PATH);
        System.out.println("-Diterations=" + ITERATIONS + ", " +
                "-Dthroughput=" + THROUGHPUT + ", " +
                "-Dpath=" + PATH);

        JLBHTask jlbhTask = new Task();

        JLBHOptions jlbhOptions = new JLBHOptions()
                .warmUpIterations(1_000_000)
                .iterations(ITERATIONS)
                .throughput(THROUGHPUT)
                .recordOSJitter(false)
                .skipFirstRun(true)
                //  .accountForCoordinatedOmission(true)
                .jlbhTask(jlbhTask)
                .runs(3);

        new JLBH(jlbhOptions).start();
    }


    @SuppressWarnings({"FieldCanBeLocal", "resource"})
    static class Task implements JLBHTask {

        private final BinaryWire wire;
        private final JBinaryBigDecimalDepthItem source = new JBinaryBigDecimalDepthItem();
        private final JBinaryBigDecimalDepthItem target = new JBinaryBigDecimalDepthItem();
        private JLBH jlbh;


        public Task() {
            Bytes bytes = Bytes.allocateElasticOnHeap();
            wire = new BinaryWire(bytes);

            source.volumeBigDecimal = new BigDecimal(100.0);
            source.priceBigDecimal = new BigDecimal(100.0);
        }

        /**
         * Initializes the JLBHTask with the provided JLBH instance. Sets up the event loop and services for benchmarking.
         *
         * @param jlbh The JLBH instance used for latency measurements.
         */
        @Override
        public void init(JLBH jlbh) {
            this.jlbh = jlbh;
        }


        /**
         * Executes a single run of the benchmark, simulating an event being processed.
         *
         * @param startTimeNS The start time of the event in nanoseconds.
         */
        @Override
        public void run(long startTimeNS) {
            wire.clear();
            wire.write("data").object(source);
            wire.read("data").object(target, JBinaryBigDecimalDepthItem.class);
            System.out.println("target = " + target);
            jlbh.sample(System.nanoTime() - startTimeNS);
        }
    }

}

