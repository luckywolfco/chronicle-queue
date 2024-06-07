package co.luckywolf.benchmark.md.binarymarshable;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.util.BinaryLengthLength;
import net.openhft.chronicle.wire.BytesInBinaryMarshallable;

import java.math.BigDecimal;

public class JBinaryBigDecimalDepthItem extends BytesInBinaryMarshallable {
    public long timestampNs = 0;
    public BigDecimal priceBigDecimal = null;
    public BigDecimal volumeBigDecimal= null;

    public String price = "0";
    public String volume = "0";

    public void setPrice(String price) {
        priceBigDecimal = null;
        this.price = price;
    }

    public void setVolume(String volume) {
        volumeBigDecimal = null;
        this.volume = volume;
    }

   public JBinaryBigDecimalDepthItem(BigDecimal price, BigDecimal volume) {
        this.priceBigDecimal = price;
        this.volumeBigDecimal = volume;
    }

    public JBinaryBigDecimalDepthItem(String price, String volume) {
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
        bytes.writeBigDecimal(volumeBigDecimal);
        bytes.writeBigDecimal(priceBigDecimal);
    }

    @Override
    public void readMarshallable(BytesIn<?> bytes)  {
        timestampNs = bytes.readLong();
        volumeBigDecimal = bytes.readBigDecimal();
        priceBigDecimal = bytes.readBigDecimal();
    }

    @Override
    public BinaryLengthLength binaryLengthLength() {
      return BinaryLengthLength.LENGTH_16BIT;
    }
}
