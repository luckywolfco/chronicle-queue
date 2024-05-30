package co.luckywolf.benchmark;

import static co.luckywolf.benchmark.JCommand.pong;

class JReadPingWritePong implements  JCommandQueueHandler.PingStatusHandler{
    private final JCommandQueueHandler.PongStatusHandler pongStatusHandler;

    public JReadPingWritePong(JCommandQueueHandler.PongStatusHandler pongStatusHandler)  {
        this.pongStatusHandler = pongStatusHandler;
    }

    @Override
    public void ping(JPing ping) {
        pong.traceId = ping.traceId;
        pong.origin = ping.origin;
        pong.service = ping.service;
        pong.commandId = System.nanoTime();
        pongStatusHandler.onPong(pong);
    }
}
