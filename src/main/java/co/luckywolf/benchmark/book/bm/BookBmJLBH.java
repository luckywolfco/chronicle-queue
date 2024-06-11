package co.luckywolf.benchmark.book.bm;


import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.jlbh.JLBH;
import net.openhft.chronicle.jlbh.JLBHOptions;
import net.openhft.chronicle.jlbh.JLBHTask;
import net.openhft.chronicle.wire.BinaryWire;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class BookBmJLBH {

    static final int ITERATIONS = Integer.getInteger("iterations", 1_000_000);
    static final int THROUGHPUT = Integer.getInteger("throughput", 200_000);
    static final String PATH = Jvm.getProperty("path", OS.getTarget() + "/data");

    /**
     * Main method to start the JLBH benchmark.
     * Configures JLBHOptions with predefined settings and initiates the benchmark.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("Running " + BookBmJLBH.class.getName() + " ...");
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
        private final BookBM source = new BookBM();
        private final BookBM target = new BookBM();
        private JLBH jlbh;


        public Task() {
            Bytes bytes = Bytes.allocateElasticOnHeap();
            wire = new BinaryWire(bytes);
            setupData(source);
        }

        private void setupData(BookBM source1) {
            source.instrument("EUR/USD");
            for (int i = 0; i < 50; i++) {
                source1.addAsk(generateRandomDouble(), generateRandomDouble());
                source1.addBid(generateRandomDouble(), generateRandomDouble());
            }
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
            long start = System.nanoTime();
            wire.clear();
            wire.write("data").object(source);
            wire.read("data").object(target, BookBM.class);
            jlbh.sample(System.nanoTime() - start);
        }

        public static double generateRandomDouble() {
            // Create an instance of Random
            Random random = new Random();

            // Generate a random double between 0 and 100,000
            double randomDouble = 0 + (10000 * random.nextDouble());

            // Round the double to up to 5 decimal places
            BigDecimal bd = new BigDecimal(Double.toString(randomDouble));
            bd = bd.setScale(5, RoundingMode.HALF_UP);

            // Return the rounded double as a primitive type
            return bd.doubleValue();
        }

    }


}

