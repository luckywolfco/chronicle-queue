package co.luckywolf.benchmark

import net.openhft.chronicle.core.Jvm
import net.openhft.chronicle.core.OS
import net.openhft.chronicle.core.io.IOTools
import net.openhft.chronicle.core.util.Time
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.RollCycles
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream


object MdBinary2ThroughputMain {

    val md = MarketDepthBinary2()

    fun md(): MarketDepthBinary2 {
        md.instrument = Instrument.VALR_BTC_ZAR
        md.service = Service.VALR_SOURCE_MARKET_DATA

        val asks = ArrayList(Data.expectedAsks)
        val bids = ArrayList(Data.expectedBids)

        md.askQty1 = asks[0].volumeBigDecimal()
        md.askPrice1 = asks[0].priceBigDecimal()

        md.askQty2 = asks[1].volumeBigDecimal()
        md.askPrice2 = asks[1].priceBigDecimal()

        md.askQty3 = asks[2].volumeBigDecimal()
        md.askPrice3 = asks[2].priceBigDecimal()

        md.askQty4 = asks[3].volumeBigDecimal()
        md.askPrice4 = asks[3].priceBigDecimal()

        md.askQty5 = asks[4].volumeBigDecimal()
        md.askPrice5 = asks[4].priceBigDecimal()
        
        md.askQty6 = asks[5].volumeBigDecimal()
        md.askPrice6 = asks[5].priceBigDecimal()

        md.askQty7 = asks[6].volumeBigDecimal()
        md.askPrice7 = asks[6].priceBigDecimal()
        
        md.askQty8 = asks[7].volumeBigDecimal()
        md.askPrice8 = asks[7].priceBigDecimal()
        
        md.askQty9 = asks[8].volumeBigDecimal()
        md.askPrice9 = asks[8].priceBigDecimal()
        
        md.askQty10 = asks[9].volumeBigDecimal()
        md.askPrice10 = asks[9].priceBigDecimal()

        md.askQty11 = asks[10].volumeBigDecimal()
        md.askPrice11 = asks[10].priceBigDecimal()

        md.askQty12 = asks[11].volumeBigDecimal()
        md.askPrice12 = asks[11].priceBigDecimal()

        md.askQty13 = asks[12].volumeBigDecimal()
        md.askPrice13 = asks[12].priceBigDecimal()

        md.askQty14 = asks[13].volumeBigDecimal()
        md.askPrice14 = asks[13].priceBigDecimal()

        md.askQty15 = asks[14].volumeBigDecimal()
        md.askPrice15 = asks[14].priceBigDecimal()

        md.askQty16 = asks[15].volumeBigDecimal()
        md.askPrice16 = asks[15].priceBigDecimal()

        md.askQty17 = asks[16].volumeBigDecimal()
        md.askPrice17 = asks[16].priceBigDecimal()

        md.askQty18 = asks[17].volumeBigDecimal()
        md.askPrice18 = asks[17].priceBigDecimal()

        md.askQty19 = asks[18].volumeBigDecimal()
        md.askPrice19 = asks[18].priceBigDecimal()

        md.askQty20 = asks[19].volumeBigDecimal()
        md.askPrice20 = asks[19].priceBigDecimal()

        md.askQty21 = asks[20].volumeBigDecimal()
        md.askPrice21 = asks[20].priceBigDecimal()

        md.askQty22 = asks[21].volumeBigDecimal()
        md.askPrice22 = asks[21].priceBigDecimal()

        md.askQty23 = asks[22].volumeBigDecimal()
        md.askPrice23 = asks[22].priceBigDecimal()

        md.askQty24 = asks[23].volumeBigDecimal()
        md.askPrice24 = asks[23].priceBigDecimal()

        md.askQty25 = asks[24].volumeBigDecimal()
        md.askPrice25 = asks[24].priceBigDecimal()

        md.askQty26 = asks[25].volumeBigDecimal()
        md.askPrice26 = asks[25].priceBigDecimal()

        md.askQty27 = asks[26].volumeBigDecimal()
        md.askPrice27 = asks[26].priceBigDecimal()

        md.askQty28 = asks[27].volumeBigDecimal()
        md.askPrice28 = asks[27].priceBigDecimal()

        md.askQty29 = asks[28].volumeBigDecimal()
        md.askPrice29 = asks[28].priceBigDecimal()

        md.askQty30 = asks[29].volumeBigDecimal()
        md.askPrice30 = asks[29].priceBigDecimal()

        md.askQty31 = asks[30].volumeBigDecimal()
        md.askPrice31 = asks[30].priceBigDecimal()

        md.askQty32 = asks[31].volumeBigDecimal()
        md.askPrice32 = asks[31].priceBigDecimal()

        md.askQty33 = asks[32].volumeBigDecimal()
        md.askPrice33 = asks[32].priceBigDecimal()

        md.askQty34 = asks[33].volumeBigDecimal()
        md.askPrice34 = asks[33].priceBigDecimal()

        md.askQty35 = asks[34].volumeBigDecimal()
        md.askPrice35 = asks[34].priceBigDecimal()

        md.askQty36 = asks[35].volumeBigDecimal()
        md.askPrice36 = asks[35].priceBigDecimal()

        md.askQty37 = asks[36].volumeBigDecimal()
        md.askPrice37 = asks[36].priceBigDecimal()

        md.askQty38 = asks[37].volumeBigDecimal()
        md.askPrice38 = asks[37].priceBigDecimal()

        md.askQty39 = asks[38].volumeBigDecimal()
        md.askPrice39 = asks[38].priceBigDecimal()

        md.askQty40 = asks[39].volumeBigDecimal()
        md.askPrice40 = asks[39].priceBigDecimal()

        md.askQty41 = asks[40].volumeBigDecimal()
        md.askPrice41 = asks[40].priceBigDecimal()

        md.askQty42 = asks[41].volumeBigDecimal()
        md.askPrice42 = asks[41].priceBigDecimal()

        md.askQty43 = asks[42].volumeBigDecimal()
        md.askPrice43 = asks[42].priceBigDecimal()

        md.askQty44 = asks[43].volumeBigDecimal()
        md.askPrice44 = asks[43].priceBigDecimal()

        md.askQty45 = asks[44].volumeBigDecimal()
        md.askPrice45 = asks[44].priceBigDecimal()

        md.askQty46 = asks[45].volumeBigDecimal()
        md.askPrice46 = asks[45].priceBigDecimal()

        md.askQty47 = asks[46].volumeBigDecimal()
        md.askPrice47 = asks[46].priceBigDecimal()

        md.askQty48 = asks[47].volumeBigDecimal()
        md.askPrice48 = asks[47].priceBigDecimal()

        md.askQty49 = asks[48].volumeBigDecimal()
        md.askPrice49 = asks[48].priceBigDecimal()

        md.askQty50 = asks[49].volumeBigDecimal()
        md.askPrice50 = asks[49].priceBigDecimal()

        md.bidQty1 = bids[0].volumeBigDecimal()
        md.bidPrice1 = bids[0].priceBigDecimal()

        md.bidQty2 = bids[1].volumeBigDecimal()
        md.bidPrice2 = bids[1].priceBigDecimal()

        md.bidQty3 = bids[2].volumeBigDecimal()
        md.bidPrice3 = bids[2].priceBigDecimal()

        md.bidQty4 = bids[3].volumeBigDecimal()
        md.bidPrice4 = bids[3].priceBigDecimal()

        md.bidQty5 = bids[4].volumeBigDecimal()
        md.bidPrice5 = bids[4].priceBigDecimal()

        md.bidQty6 = bids[5].volumeBigDecimal()
        md.bidPrice6 = bids[5].priceBigDecimal()

        md.bidQty7 = bids[6].volumeBigDecimal()
        md.bidPrice7 = bids[6].priceBigDecimal()

        md.bidQty8 = bids[7].volumeBigDecimal()
        md.bidPrice8 = bids[7].priceBigDecimal()

        md.bidQty9 = bids[8].volumeBigDecimal()
        md.bidPrice9 = bids[8].priceBigDecimal()

        md.bidQty10 = bids[9].volumeBigDecimal()
        md.bidPrice10 = bids[9].priceBigDecimal()

        md.bidQty11 = bids[10].volumeBigDecimal()
        md.bidPrice11 = bids[10].priceBigDecimal()

        md.bidQty12 = bids[11].volumeBigDecimal()
        md.bidPrice12 = bids[11].priceBigDecimal()

        md.bidQty13 = bids[12].volumeBigDecimal()
        md.bidPrice13 = bids[12].priceBigDecimal()

        md.bidQty14 = bids[13].volumeBigDecimal()
        md.bidPrice14 = bids[13].priceBigDecimal()

        md.bidQty15 = bids[14].volumeBigDecimal()
        md.bidPrice15 = bids[14].priceBigDecimal()

        md.bidQty16 = bids[15].volumeBigDecimal()
        md.bidPrice16 = bids[15].priceBigDecimal()

        md.bidQty17 = bids[16].volumeBigDecimal()
        md.bidPrice17 = bids[16].priceBigDecimal()

        md.bidQty18 = bids[17].volumeBigDecimal()
        md.bidPrice18 = bids[17].priceBigDecimal()

        md.bidQty19 = bids[18].volumeBigDecimal()
        md.bidPrice19 = bids[18].priceBigDecimal()

        md.bidQty20 = bids[19].volumeBigDecimal()
        md.bidPrice20 = bids[19].priceBigDecimal()

        md.bidQty21 = bids[20].volumeBigDecimal()
        md.bidPrice21 = bids[20].priceBigDecimal()

        md.bidQty22 = bids[21].volumeBigDecimal()
        md.bidPrice22 = bids[21].priceBigDecimal()

        md.bidQty23 = bids[22].volumeBigDecimal()
        md.bidPrice23 = bids[22].priceBigDecimal()

        md.bidQty24 = bids[23].volumeBigDecimal()
        md.bidPrice24 = bids[23].priceBigDecimal()

        md.bidQty25 = bids[24].volumeBigDecimal()
        md.bidPrice25 = bids[24].priceBigDecimal()

        md.bidQty26 = bids[25].volumeBigDecimal()
        md.bidPrice26 = bids[25].priceBigDecimal()

        md.bidQty27 = bids[26].volumeBigDecimal()
        md.bidPrice27 = bids[26].priceBigDecimal()

        md.bidQty28 = bids[27].volumeBigDecimal()
        md.bidPrice28 = bids[27].priceBigDecimal()

        md.bidQty29 = bids[28].volumeBigDecimal()
        md.bidPrice29 = bids[28].priceBigDecimal()

        md.bidQty30 = bids[29].volumeBigDecimal()
        md.bidPrice30 = bids[29].priceBigDecimal()

        md.bidQty31 = bids[30].volumeBigDecimal()
        md.bidPrice31 = bids[30].priceBigDecimal()

        md.bidQty32 = bids[31].volumeBigDecimal()
        md.bidPrice32 = bids[31].priceBigDecimal()

        md.bidQty33 = bids[32].volumeBigDecimal()
        md.bidPrice33 = bids[32].priceBigDecimal()

        md.bidQty34 = bids[33].volumeBigDecimal()
        md.bidPrice34 = bids[33].priceBigDecimal()

        md.bidQty35 = bids[34].volumeBigDecimal()
        md.bidPrice35 = bids[34].priceBigDecimal()

        md.bidQty36 = bids[35].volumeBigDecimal()
        md.bidPrice36 = bids[35].priceBigDecimal()

        md.bidQty37 = bids[36].volumeBigDecimal()
        md.bidPrice37 = bids[36].priceBigDecimal()

        md.bidQty38 = bids[37].volumeBigDecimal()
        md.bidPrice38 = bids[37].priceBigDecimal()

        md.bidQty39 = bids[38].volumeBigDecimal()
        md.bidPrice39 = bids[38].priceBigDecimal()

        md.bidQty40 = bids[39].volumeBigDecimal()
        md.bidPrice40 = bids[39].priceBigDecimal()

        md.bidQty41 = bids[40].volumeBigDecimal()
        md.bidPrice41 = bids[40].priceBigDecimal()

        md.bidQty42 = bids[41].volumeBigDecimal()
        md.bidPrice42 = bids[41].priceBigDecimal()

        md.bidQty43 = bids[42].volumeBigDecimal()
        md.bidPrice43 = bids[42].priceBigDecimal()

        md.bidQty44 = bids[43].volumeBigDecimal()
        md.bidPrice44 = bids[43].priceBigDecimal()

        md.bidQty45 = bids[44].volumeBigDecimal()
        md.bidPrice45 = bids[44].priceBigDecimal()

        md.bidQty46 = bids[45].volumeBigDecimal()
        md.bidPrice46 = bids[45].priceBigDecimal()

        md.bidQty47 = bids[46].volumeBigDecimal()
        md.bidPrice47 = bids[46].priceBigDecimal()

        md.bidQty48 = bids[47].volumeBigDecimal()
        md.bidPrice48 = bids[47].priceBigDecimal()

        md.bidQty49 = bids[48].volumeBigDecimal()
        md.bidPrice49 = bids[48].priceBigDecimal()

        md.bidQty50 = bids[49].volumeBigDecimal()
        md.bidPrice50 = bids[49].priceBigDecimal()


//        expectedAsks.forEach { md.asks[it.priceBigDecimal()] = it }
//        expectedBids.forEach { md.bids[it.priceBigDecimal()] = it }
        return md
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(
            "Testing with " +
                    "-Dtime=" + EchoBenchmarkMain.time + " " +
                    "-Dthreads=" + EchoBenchmarkMain.threads + " " +
                    "-Dsize=" + EchoBenchmarkMain.size + " " +
                    "-Dpath=" + EchoBenchmarkMain.path + " " +
                    "-DfullWrite=" + EchoBenchmarkMain.fullWrite
        )

        val start = System.nanoTime()
        val base = EchoBenchmarkMain.path + "/delete-" + Time.uniqueId() + ".me."

        val blockSize = if (OS.is64Bit())
            if (OS.isLinux()) 4L shl 30
            else 1L shl 30
        else 256L shl 20

        val count = AtomicLong()
        IntStream.range(0, EchoBenchmarkMain.threads).parallel().forEach { i: Int ->
            var count2: Long = 0

            ChronicleQueue.singleBuilder(base + i)
                .rollCycle(RollCycles.FAST_HOURLY)
                .blockSize(blockSize)
                .build().use { q ->
                    val appender = q.acquireAppender()
                    val writer = appender.methodWriter(CommandQueueHandler.MarketDataBinary2Handler::class.java)
                    var lastIndex: Long = -1
                    val md = md()
                    do {
                        md.traceId = System.nanoTime()
                        md.commandId = md.traceId
                        writer.onMarketData(md)
                        count2++
                    } while (start + EchoBenchmarkMain.time * 1e9 > System.nanoTime())
                }
            count.addAndGet(count2)
        }
        val time1 = System.nanoTime() - start
        Jvm.pause(1000)
        System.gc()
        val mid = System.nanoTime()
        IntStream.range(0, EchoBenchmarkMain.threads).parallel().forEach { i: Int ->
            ChronicleQueue.singleBuilder(base + i)
                .rollCycle(RollCycles.FAST_HOURLY)
                .blockSize(blockSize)
                .build().use { q ->
                    val tailer = q.createTailer()
                    val pingReader = tailer.methodReader(Echo.ReadBinary2MarketData())
                    while (pingReader.readOne()) {}
                }
        }
        val end = System.nanoTime()
        val time2 = end - mid

        System.out.printf(
            "Writing %,d messages took %.3f seconds, at a rate of %,d per second%n",
            count.toLong(), time1 / 1e9, 1000 * (1e6 * count.get() / time1).toLong()
        )
        System.out.printf(
            "Reading %,d messages took %.3f seconds, at a rate of %,d per second%n",
            count.toLong(), time2 / 1e9, 1000 * (1e6 * count.get() / time2).toLong()
        )

        Jvm.pause(200)
        System.gc() // make sure its cleaned up for windows to delete.
        IntStream.range(0, EchoBenchmarkMain.threads).forEach { i: Int ->
            IOTools.deleteDirWithFiles(base + i, 2)
        }
    }
}