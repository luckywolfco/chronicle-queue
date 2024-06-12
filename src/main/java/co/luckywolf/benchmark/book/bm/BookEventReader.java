package co.luckywolf.benchmark.book.bm;

public class BookEventReader implements BookEvent {
    private long timeStampNs;
    @Override
    public void process(BookBM book) {
        this.timeStampNs = book.getTimeStampNs();
    }
}
