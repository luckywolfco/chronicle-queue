package co.luckywolf.benchmark.md.selfdescribing;

import net.openhft.chronicle.wire.SelfDescribingMarshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

import java.math.BigDecimal;

public class JBigDecimalDepthItem extends SelfDescribingMarshallable {
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

   public JBigDecimalDepthItem(BigDecimal price, BigDecimal volume) {
        this.priceBigDecimal = price;
        this.volumeBigDecimal = volume;
    }

    public JBigDecimalDepthItem(String price, String volume) {
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
    public void writeMarshallable(WireOut wire) {
        wire.write("t").writeLong(timestampNs);
        wire.write("p").writeString(price);
        wire.write("v").writeString(volume);
    }

    @Override
    public void readMarshallable(WireIn wire) {
        timestampNs = wire.read("t").readLong();
        price = wire.read("p").readString();
        volume = wire.read("v").readString();
    }
}
