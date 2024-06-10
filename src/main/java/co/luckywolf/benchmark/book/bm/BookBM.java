package co.luckywolf.benchmark.book.bm;

import co.luckywolf.benchmark.book.tc.Rung;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.BytesUtil;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Double.NaN;
import static net.openhft.chronicle.bytes.internal.BytesFieldInfo.lookup;
import static net.openhft.chronicle.core.Jvm.fieldOffset;
import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

/**
 * This is sent every time there is a change on the book that can cause it to reevaluate
 */
public class BookBM implements BytesMarshallable, Marshallable {

    private static final int DESCRIPTION = lookup(BookBM.class).description();
//    private static final int LENGTH, START;
//    private static final long START_OF_BIDS_PRICE = fieldOffset(BookBM.class, "bidPrice0");
//    private static final long START_OF_BIDS_VOLUME = fieldOffset(BookBM.class, "bidVolume0");
//    private static final long START_OF_ASKS_PRICE = fieldOffset(BookBM.class, "askPrice0");
//    private static final long START_OF_ASKS_VOLUME = fieldOffset(BookBM.class, "askVolume0");
//
//    static {
//        final int[] range = BytesUtil.triviallyCopyableRange(BookBM.class);
//        LENGTH = range[1] - range[0];
//        START = range[0];
//    }

    @LongConversion(ShortTextLongConverter.class)
    private long instrument;

    private int version;

    // bid
    public int bidCount;
    private double[] bidPrices = new double[50];
    private double[] bidVolumes = new double[50];

    // ask
    private int askCount;
    private double[] askPrices = new double[50];
    private double[] askVolumes = new double[50];

    public double getBidPrice(int index) {
        return bidPrices[index];
    }

    public double getBidVolume(int index) {
        return bidVolumes[index];
    }

    public double getAskPrice(int index) {
        return askPrices[index];
    }

    public double getAskVolume(int index) {
        return askVolumes[index];
    }

    public CharSequence instrument() {
        return ShortTextLongConverter.INSTANCE.asString(instrument);
    }

    public void instrument(CharSequence instrument) {
        this.instrument = ShortTextLongConverter.INSTANCE.parse(instrument);
    }

    public BookBM addBid(double price, double volume) {
        if(bidCount >= bidPrices.length) {
            bidPrices = Arrays.copyOf(bidPrices, bidPrices.length * 2);
            bidVolumes = Arrays.copyOf(bidVolumes, bidVolumes.length * 2);
        }
        setBidPrice(bidCount, price);
        setBidVolume(bidCount, volume);
        bidCount++;
        return this;
    }

    public void addAsk(double price, double volume) {
        if(askCount >= askPrices.length) {
            askPrices = Arrays.copyOf(askPrices, askPrices.length * 2);
            askVolumes = Arrays.copyOf(askVolumes, askVolumes.length * 2);
        }
        setAskPrice(askCount, price);
        setAskVolume(askCount, volume);
        askCount++;
    }

    public BookBM setBidPrice(int index, double price) {
        bidPrices[index] = price;
        return this;
    }

    public BookBM setBidVolume(int index, double volume) {
        bidVolumes[index] = volume;
        return this;
    }

    public BookBM setAskPrice(int index, double price) {
        askPrices[index] = price;
        return this;
    }

    public BookBM setAskVolume(int index, double volume) {
        askVolumes[index] = volume;
        return this;
    }


    private List<Rung> addBidRungs(int count) {
        final List<Rung> rungs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rungs.add(new Rung(getBidPrice(i), getBidVolume(i)));
        }
        return rungs;
    }

    private List<Rung> addAskRungs(int count) {
        final List<Rung> rungs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rungs.add(new Rung(getAskPrice(i), getAskVolume(i)));
        }
        return rungs;
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) throws InvalidMarshallableException {
        wire.write("instrument").text(ShortTextLongConverter.INSTANCE.asText(instrument));
        wire.write("bidCount").int32(bidCount);
        wire.write("askCount").int32(askCount);
        wire.write("bids").list(addBidRungs(bidCount), Rung.class);
        wire.write("asks").list(addAskRungs(askCount), Rung.class);
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        instrument = wire.read("instrument").int64();
        StringBuilder sb = new StringBuilder();
        wire.read("instrument").text(sb);
        instrument = ShortTextLongConverter.INSTANCE.parse(sb);

        bidCount = wire.read("bidCount").int32();
        askCount = wire.read("askCount").int32();
        bidCount = readRungs(wire, "bid", this::setAllBidRungs);
        askCount = readRungs(wire, "ask", this::setAllAskRungs);
    }

    private void setAllAskRungs(Iterable<Rung> rungs) {
        int i = 0;
        for (Rung rung : rungs) {
            setAskPrice(i, rung.price()).setAskVolume(i, rung.volume());
            i++;
        }
        for (; i <= 9; i++) {
            setAskPrice(i, NaN).setAskVolume(i, 0);
        }
    }

    private void setAllBidRungs(Iterable<Rung> rungs) {
        int i = 0;
        for (Rung rung : rungs) {
            setBidPrice(i, rung.price()).setBidVolume(i, rung.volume());
            i++;
        }
        for (; i <= 9; i++) {
            setBidPrice(i, NaN).setBidVolume(i, 0);
        }
    }


    private byte readRungs(@NotNull WireIn wire, final String side, Consumer<List<Rung>> setRungs) {
        final List<Rung> rungs = new ArrayList<>();
        wire.read(side + "s").sequence(this, (t, v) -> {
            rungs.clear();
            while (v.hasNextSequenceItem()) {
                Rung r = new Rung();
                v.marshallable(r);
                rungs.add(r);
            }
        });
        setRungs.accept(rungs);
        return (byte) rungs.size();
    }

    protected int $description() {
        return DESCRIPTION;
    }

    @Override
    public void readMarshallable(BytesIn<?> bytes) throws IORuntimeException, BufferUnderflowException, IllegalStateException {
        int description0 = bytes.readInt();
        if (description0 == this.$description()) {
            instrument = bytes.readLong();
            version = bytes.readInt();
            askCount = 0;
            int askCount = bytes.readInt();
            for (int i = 0; i < askCount; i++) {
                addAsk(bytes.readDouble(), bytes.readDouble());
            }
            bidCount = 0;
            int bidCount = bytes.readInt();
            for (int i = 0; i < bidCount; i++) {
                addBid(bytes.readDouble(), bytes.readDouble());
            }
//            bytes.unsafeReadObject(this, this.$start(), this.$length());
        } else {
            throw new InvalidMarshallableException("Description mismatch, expected " + this.$description() + ", was " + description0);
        }
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, ArithmeticException {
        bytes.writeInt(this.$description());
        bytes.writeLong(instrument);
        bytes.writeInt(version);
        bytes.writeInt(askCount);
        for (int i = 0; i < askCount; i++) {
            bytes.writeDouble(askPrices[i]);
            bytes.writeDouble(askVolumes[i]);
        }
        bytes.writeInt(bidCount);
        for (int i = 0; i < bidCount; i++) {
            bytes.writeDouble(bidPrices[i]);
            bytes.writeDouble(bidVolumes[i]);
        }
//        bytes.unsafeWriteObject(this, this.$start(), this.$length());
    }


    @Override
    public String toString() {
        return Marshallable.$toString(this);
    }
}


