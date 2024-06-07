package co.luckywolf.benchmark.book.tc;

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
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Double.NaN;
import static net.openhft.chronicle.bytes.internal.BytesFieldInfo.lookup;
import static net.openhft.chronicle.core.Jvm.fieldOffset;
import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

/**
 * This is sent every time there is a change on the book that can cause it to reevaluate
 */
public class BookTC implements BytesMarshallable, Marshallable {


    private static final int DESCRIPTION = lookup(BookTC.class).description();
    private static final int LENGTH, START;
    private static final long START_OF_BIDS_PRICE = fieldOffset(BookTC.class, "bidPrice0");
    private static final long START_OF_BIDS_VOLUME = fieldOffset(BookTC.class, "bidVolume0");
    private static final long START_OF_ASKS_PRICE = fieldOffset(BookTC.class, "askPrice0");
    private static final long START_OF_ASKS_VOLUME = fieldOffset(BookTC.class, "askVolume0");

    static {
        final int[] range = BytesUtil.triviallyCopyableRange(BookTC.class);
        LENGTH = range[1] - range[0];
        START = range[0];
    }

    @LongConversion(ShortTextLongConverter.class)
    private long instrument;


    // bid
    public int bidCount = 0;
    public double bidPrice0, bidPrice1, bidPrice2, bidPrice3, bidPrice4, bidPrice5, bidPrice6, bidPrice7, bidPrice8, bidPrice9,
            bidPrice10, bidPrice11, bidPrice12, bidPrice13, bidPrice14, bidPrice15, bidPrice16, bidPrice17, bidPrice18, bidPrice19,
            bidPrice20, bidPrice21, bidPrice22, bidPrice23, bidPrice24, bidPrice25, bidPrice26, bidPrice27, bidPrice28, bidPrice29,
            bidPrice30, bidPrice31, bidPrice32, bidPrice33, bidPrice34, bidPrice35, bidPrice36, bidPrice37, bidPrice38, bidPrice39,
            bidPrice40, bidPrice41, bidPrice42, bidPrice43, bidPrice44, bidPrice45, bidPrice46, bidPrice47, bidPrice48, bidPrice49;

    public double bidVolume0, bidVolume1, bidVolume2, bidVolume3, bidVolume4, bidVolume5, bidVolume6, bidVolume7, bidVolume8, bidVolume9,
            bidVolume10, bidVolume11, bidVolume12, bidVolume13, bidVolume14, bidVolume15, bidVolume16, bidVolume17, bidVolume18, bidVolume19,
            bidVolume20, bidVolume21, bidVolume22, bidVolume23, bidVolume24, bidVolume25, bidVolume26, bidVolume27, bidVolume28, bidVolume29,
            bidVolume30, bidVolume31, bidVolume32, bidVolume33, bidVolume34, bidVolume35, bidVolume36, bidVolume37, bidVolume38, bidVolume39,
            bidVolume40, bidVolume41, bidVolume42, bidVolume43, bidVolume44, bidVolume45, bidVolume46, bidVolume47, bidVolume48, bidVolume49;

    // ask
    public int askCount;
    public double askPrice0, askPrice1, askPrice2, askPrice3, askPrice4, askPrice5, askPrice6, askPrice7, askPrice8, askPrice9,
            askPrice10, askPrice11, askPrice12, askPrice13, askPrice14, askPrice15, askPrice16, askPrice17, askPrice18, askPrice19,
            askPrice20, askPrice21, askPrice22, askPrice23, askPrice24, askPrice25, askPrice26, askPrice27, askPrice28, askPrice29,
            askPrice30, askPrice31, askPrice32, askPrice33, askPrice34, askPrice35, askPrice36, askPrice37, askPrice38, askPrice39,
            askPrice40, askPrice41, askPrice42, askPrice43, askPrice44, askPrice45, askPrice46, askPrice47, askPrice48, askPrice49;

    public double askVolume0, askVolume1, askVolume2, askVolume3, askVolume4, askVolume5, askVolume6, askVolume7, askVolume8, askVolume9,
            askVolume10, askVolume11, askVolume12, askVolume13, askVolume14, askVolume15, askVolume16, askVolume17, askVolume18, askVolume19,
            askVolume20, askVolume21, askVolume22, askVolume23, askVolume24, askVolume25, askVolume26, askVolume27, askVolume28, askVolume29,
            askVolume30, askVolume31, askVolume32, askVolume33, askVolume34, askVolume35, askVolume36, askVolume37, askVolume38, askVolume39,
            askVolume40, askVolume41, askVolume42, askVolume43, askVolume44, askVolume45, askVolume46, askVolume47, askVolume48, askVolume49;


    public double getBidPrice(int index) {
        return getDoubleAtOffsetIndex(index, START_OF_BIDS_PRICE);
    }

    public double getBidVolume(int index) {
        return getDoubleAtOffsetIndex(index, START_OF_BIDS_VOLUME);
    }

    public double getAskPrice(int index) {
        return getDoubleAtOffsetIndex(index, START_OF_ASKS_PRICE);
    }

    public double getAskVolume(int index) {
        return getDoubleAtOffsetIndex(index, START_OF_ASKS_VOLUME);
    }

    private double getDoubleAtOffsetIndex(int index, final long address) {
        return MEMORY.readDouble(this, address + index * 8L);
    }

    private BookTC setDoubleAtOffsetIndex(int index, double Price, final long address) {
        MEMORY.writeDouble(this, address + index * 8L, Price);
        return this;
    }

    public CharSequence instrument() {
        return ShortTextLongConverter.INSTANCE.asString(instrument);
    }

    public void instrument(CharSequence instrument) {
        this.instrument = ShortTextLongConverter.INSTANCE.parse(instrument);
    }

    public BookTC addBid(double price, double volume) {
        setBidPrice(bidCount, price);
        setBidVolume(bidCount, volume);
        bidCount++;
        return this;
    }

    public void addAsk(double price, double volume) {
        setDoubleAtOffsetIndex(askCount, price, START_OF_ASKS_PRICE);
        setDoubleAtOffsetIndex(askCount, volume, START_OF_ASKS_VOLUME);
        askCount++;
    }

    public BookTC setBidPrice(int index, double Price) {
        return setDoubleAtOffsetIndex(index, Price, START_OF_BIDS_PRICE);
    }

    public BookTC setBidVolume(int index, double volume) {
        return setDoubleAtOffsetIndex(index, volume, START_OF_BIDS_VOLUME);
    }

    public BookTC setAskPrice(int index, double price) {
        return setDoubleAtOffsetIndex(index, price, START_OF_ASKS_PRICE);
    }

    public BookTC setAskVolume(int index, double volume) {
        return setDoubleAtOffsetIndex(index, volume, START_OF_ASKS_VOLUME);
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
        wire.write("bidCount").int32(bidCount);
        wire.write("askCount").int32(askCount);
        wire.write("bids").list(addBidRungs(bidCount), Rung.class);
        wire.write("asks").list(addAskRungs(askCount), Rung.class);
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
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


    protected int $start() {
        return START;
    }


    protected int $length() {
        return LENGTH;
    }

    @Override
    public void readMarshallable(BytesIn<?> bytes) throws IORuntimeException, BufferUnderflowException, IllegalStateException {
        int description0 = bytes.readInt();
        if (description0 == this.$description()) {
            bytes.unsafeReadObject(this, this.$start(), this.$length());
        } else {
            throw new InvalidMarshallableException("Description mismatch, expected " + this.$description() + ", was " + description0);
        }
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, ArithmeticException {
        bytes.writeInt(this.$description());
        bytes.unsafeWriteObject(this, this.$start(), this.$length());
    }


    @Override
    public String toString() {
        return Marshallable.$toString(this);
    }
}


