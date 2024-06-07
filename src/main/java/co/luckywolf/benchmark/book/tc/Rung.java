package co.luckywolf.benchmark.book.tc;

import net.openhft.chronicle.wire.SelfDescribingMarshallable;

public class Rung extends SelfDescribingMarshallable {
    private double price;
    private double volume;

    public Rung() {
    }

    public Rung(double bidRate, double bidQty) {
        this.price = bidRate;
        this.volume = bidQty;
    }

    public double price() {
        return price;
    }

    public Rung price(double price) {
        this.price = price;
        return this;
    }

    public double volume() {
        return volume;
    }

    public Rung volume(double volume) {
        this.volume = volume;
        return this;
    }
}
