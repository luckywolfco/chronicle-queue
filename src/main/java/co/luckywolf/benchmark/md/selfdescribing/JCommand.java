package co.luckywolf.benchmark.md.selfdescribing;

import co.luckywolf.benchmark.Service;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class JCommand extends SelfDescribingMarshallable {
    private long commandId = 0;
    private long traceId;
    private int version = 1;
    private Service origin = Service.UNDEFINED;

    public JCommand() {
        commandId = SystemTimeProvider.INSTANCE.currentTimeNanos();
        traceId = commandId;
    }

    public long getCommandId() {
        return commandId;
    }
    public void setCommandId(long commandId) {
        this.commandId = commandId;
    }
    public long getTraceId() {
        return traceId;
    }
    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    public Service getOrigin() {
        return origin;
    }

    public void setOrigin(Service origin) {
        this.origin = origin;
    }

    protected void clear() {
        commandId = 0;
        traceId = 0;
        version = 1;
        origin = Service.UNDEFINED;
    }
}
