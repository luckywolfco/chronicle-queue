package co.luckywolf.benchmark;

interface JCommandQueueHandler {
    interface PingStatusHandler {
        public void  ping(JPing ping);
    }

    interface PongStatusHandler {
        void onPong(JPong pong);
    }
}
