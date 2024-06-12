package co.luckywolf.benchmark.book.tc;


public class BookEventReader implements BookEvent {
    private long timeStampNs;
    @Override
    public void process(BookTC book) {
        this.timeStampNs = book.getTimeStampNs();
    }
}
