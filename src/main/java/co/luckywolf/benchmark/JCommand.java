package co.luckywolf.benchmark;

import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.NanoTimestampLongConverter;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

class JCommand extends SelfDescribingMarshallable {

    public static JPong pong = new JPong(Service.GATEWAY, PongStatus.WEBSOCKET_CONNECTED, "");

    JCommand() {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos();
        traceId = commandId;
    }

    @LongConversion(NanoTimestampLongConverter.class)
    public long commandId = 0;

    @LongConversion(NanoTimestampLongConverter.class)
    public long traceId = 0;

    public int version = 1;

    public Service origin = Service.UNDEFINED;
}
