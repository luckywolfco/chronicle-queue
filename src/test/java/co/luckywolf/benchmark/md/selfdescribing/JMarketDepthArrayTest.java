package co.luckywolf.benchmark.md.selfdescribing;

import co.luckywolf.benchmark.Service;
import co.luckywolf.benchmark.md.Data;
import co.luckywolf.benchmark.md.DepthItem;
import co.luckywolf.benchmark.md.Instrument;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JMarketDepthArrayTest {
    @Test
    public void test_serialization_and_deserialization() {
        Wire wire = WireType.BINARY.apply(Bytes.allocateElasticOnHeap());
        long timestampNs = System.nanoTime();
        JMarketDepthArray md = new JMarketDepthArray(timestampNs, Instrument.VALR_BTC_ZAR);

        md.setService(Service.VALR_SOURCE_MARKET_DATA);
        for (DepthItem item : Data.INSTANCE.getExpectedAsks()) {
            md.getAsks().add(new JStringDepthItem(item.getPrice(), item.getVolume()));
        }

        for (DepthItem item : Data.INSTANCE.getExpectedBids()) {
            md.getBids().add(new JStringDepthItem(item.getPrice(), item.getVolume()));
        }

       wire.getValueOut().object(md);

        JMarketDepthArray mdOut = wire.getValueIn().object(JMarketDepthArray.class);

        assertEquals(Instrument.VALR_BTC_ZAR, mdOut.getInstrument());
        assertEquals(timestampNs, mdOut.getTimestampNs());

//        assertEquals(Data.INSTANCE.getExpectedAsks()..map { it.volume.toBigDecimal() }, mdOut.asks.map { it.volume })
//        assertEquals(Data.expectedAsks.map { it.price.toBigDecimal() }, mdOut.asks.map { it.price })
//
//        assertEquals(Data.expectedBids.map { it.volume.toBigDecimal() }, mdOut.bids.map { it.volume })
//        assertEquals(Data.expectedBids.map { it.price.toBigDecimal() }, mdOut.bids.map { it.price })
    }
}