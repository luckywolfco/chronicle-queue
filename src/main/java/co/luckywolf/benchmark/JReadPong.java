package co.luckywolf.benchmark;

import net.openhft.chronicle.core.util.Histogram;

import static co.luckywolf.benchmark.JCommand.pong;

class JReadPong implements JCommandQueueHandler.PingStatusHandler {

    public final Histogram histogramCo;
    public final Histogram histogramIn;

    public JReadPong(Histogram histogramCo, Histogram histogramIn) {

        this.histogramCo = histogramCo;
        this.histogramIn = histogramIn;
    }

    public void ping(JPing ping) {
        long startCo = ping.traceId;
        long startIn = pong.commandId;
        long now = System.nanoTime();
        histogramCo.sample((double)(now - startCo));
        histogramIn.sample((double)(now - startIn));
    }
}
