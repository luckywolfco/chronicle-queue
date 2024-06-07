package co.luckywolf.benchmark.md.binarymarshable;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.wire.BytesInBinaryMarshallable;

import java.math.BigDecimal;

public class JBinaryStringDepthItem extends BytesInBinaryMarshallable {
    public long timestampNs = 0;
    public BigDecimal priceBigDecimal = null;
    public BigDecimal volumeBigDecimal= null;

    public String price = "0";
    public String volume = "0";

    public JBinaryStringDepthItem(String price, String volume) {
        this.price = price;
        this.volume = volume;
    }

    public BigDecimal volumeBigDecimal() {
        if (volumeBigDecimal == null) {
            volumeBigDecimal = new BigDecimal(volume);
        }
        return volumeBigDecimal;
    }

    public BigDecimal priceBigDecimal() {
        if (priceBigDecimal == null) {
            priceBigDecimal = new BigDecimal(price);
        }
        return priceBigDecimal;
    }

    @Override
    public void writeMarshallable(BytesOut<?> bytes) {
        bytes.writeLong(timestampNs);
        bytes.writeUtf8(volume);
        bytes.writeUtf8(price);
    }

    @Override
    public void readMarshallable(BytesIn<?> bytes)  {
        timestampNs = bytes.readLong();
        volume = bytes.readUtf8();
        price = bytes.readUtf8();
    }
}
