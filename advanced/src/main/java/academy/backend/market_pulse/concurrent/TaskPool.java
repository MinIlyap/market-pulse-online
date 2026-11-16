package academy.backend.market_pulse.concurrent;

public interface TaskPool extends AutoCloseable {
    void submit(Runnable task);

    @Override
    void close() throws InterruptedException;
}
