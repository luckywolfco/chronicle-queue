package co.luckywolf.benchmark;

import net.openhft.chronicle.core.util.Histogram;

class JReadPong implements JCommandQueueHandler.PongStatusHandler {

    public final Histogram histogramCo;
    public final Histogram histogramIn;

    public JReadPong(Histogram histogramCo, Histogram histogramIn) {

        this.histogramCo = histogramCo;
        this.histogramIn = histogramIn;
    }

    @Override
    public void onPong(JPong pong) {
        long startCo = pong.traceId;
        long startIn = pong.commandId;
        long now = System.nanoTime();
        histogramCo.sample((double)(now - startCo));
        histogramIn.sample((double)(now - startIn));
    }
}
