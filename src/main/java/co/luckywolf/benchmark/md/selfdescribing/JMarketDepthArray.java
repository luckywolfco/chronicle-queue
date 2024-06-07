package co.luckywolf.benchmark.md.selfdescribing;

import co.luckywolf.benchmark.Service;
import co.luckywolf.benchmark.md.Instrument;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.wire.ValueIn;
import net.openhft.chronicle.wire.ValueOut;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class JMarketDepthArray extends JCommand {
    private final ArrayList<JStringDepthItem> bids = new ArrayList(50);
    private final ArrayList<JStringDepthItem> asks = new ArrayList(50);

    private long timestampNs = 0;
    private Instrument instrument = Instrument.UNDEFINED;
    private Service service = Service.UNDEFINED;

    public JMarketDepthArray(long timestampNs, Instrument instrument) {
        this.timestampNs = timestampNs;
        this.instrument = instrument;
    }

    public JMarketDepthArray(long timestampNs, Instrument instrument, ArrayList<JStringDepthItem> bids, ArrayList<JStringDepthItem> asks){
        this.asks.clear();
        this.asks.addAll(asks);
        this.bids.clear();
        this.bids.addAll(bids);
        this.timestampNs = timestampNs;
        this.instrument = instrument;
    }

    public void clear() {
        super.clear();
        asks.clear();
        bids.clear();
        timestampNs = 0;
        instrument = Instrument.UNDEFINED;
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) throws InvalidMarshallableException {
        wire.write("ci").writeLong(getCommandId());
        wire.write("ti").writeLong(getTraceId());
        wire.write("v").writeInt(getVersion());
        wire.write("o").writeInt(getOrigin().getId());
        wire.write("t").writeLong(timestampNs);
        wire.write("i").writeInt(instrument.getId());
        wire.write("s").writeInt(service.getId());

        wire.write("a").sequence(asks, asks.size(), ::writeDepthItems);
        wire.write("b").sequence(bids, bids.size(), ::writeDepthItems);
    }


    private void writeDepthItems(ArrayList<JStringDepthItem> depthItems, int size, ValueOut valueOut) {
        for (int i = 0; i < depthItems.size(); i++) {
            valueOut.sequence(depthItems[i], ::writeItem);
        }
    }

    private void writeItem(JStringDepthItem item, ValueOut valueOut) {
        valueOut.writeString(item.getPrice());
        valueOut.writeString(item.getVolume());
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException, InvalidMarshallableException {
        setCommandId(wire.read("ci").readLong());
        setTraceId(wire.read("ti").readLong());
        setVersion(wire.read("v").readInt());
        setOrigin(Service.Companion.fromId(wire.read("o").readInt()));
        timestampNs = wire.read("t").readLong();
        instrument = Instrument.Companion.toInstrument(wire.read("i").readInt());
        service = Service.Companion.fromId(wire.read("s").readInt())
        asks.clear();
        bids.clear();

        wire.read("a").sequence(asks, ::readDepthItems)
        wire.read("b").sequence(bids, :: readDepthItems)
    }

    private void readDepthItems(ArrayList<JStringDepthItem> depthItems, ValueIn valueIn) {
        while (valueIn.hasNextSequenceItem()) {
            valueIn.sequence(depthItems,::readItem)
        }
    }
    private final StringBuilder sb = new StringBuilder();
    private void readItem(ArrayList<JStringDepthItem> depthItems, ValueIn valueIn) {
        JStringDepthItem depthItem = new JStringDepthItem(); // could maybe use the object pool
        depthItem.setPrice(valueIn.textTo(sb).toString());
        depthItem.setVolume(valueIn.textTo(sb).toString());
        depthItems.add(depthItem);
    }
}

