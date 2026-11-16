package academy.backend.market_pulse.concurrent;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public final class BlockingQueueThreadPool implements TaskPool {
    private static final System.Logger LOGGER = System.getLogger(BlockingQueueThreadPool.class.getName());

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private final Thread[] workers;
    private final Consumer<RuntimeException> onFailure;
    private volatile boolean stopped;

    public BlockingQueueThreadPool(int size, Consumer<RuntimeException> onFailure) {
        if (size <= 0) {
            throw new IllegalArgumentException("Число worker-потоков должно быть положительным");
        }

        this.onFailure = Objects.requireNonNull(onFailure);
        this.workers = new Thread[size];

        for (int index = 0; index < size; index++) {
            this.workers[index] = Thread.ofPlatform()
                    .name("quote-worker-" + (index + 1))
                    .start(this::work);
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task);

        if (stopped) {
            throw new IllegalStateException("Пул закрыт");
        }
        tasks.offer(task);
    }

    private void work() {
        while (!stopped) {
            Runnable task;

            try {
                task = tasks.take();
            } catch (InterruptedException error) {
                return;
            }

            if (!stopped) {
                execute(task);
            }
        }
    }

    private void execute(Runnable task) {
        try {
            task.run();
        } catch (RuntimeException error) {
            reportFailure(error);
        } finally {
            Thread.interrupted();
        }
    }

    private void reportFailure(RuntimeException error) {
        try {
            onFailure.accept(error);
        } catch (RuntimeException reportingError) {
            LOGGER.log(System.Logger.Level.ERROR, "Ошибка обработчика задачи", reportingError);
        }
    }

    @Override
    public void close() throws InterruptedException {
        stopped = true;
        tasks.clear();

        for (Thread worker : workers) {
            worker.interrupt();
        }
        boolean interrupted = Thread.interrupted();

        for (Thread worker : workers) {
            while (worker.isAlive()) {
                try {
                    worker.join();
                } catch (InterruptedException error) {
                    interrupted = true;
                }
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
            throw new InterruptedException("Ожидание остановки пула было прервано");
        }
    }
}
