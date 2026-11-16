package academy.backend.market_pulse.dto.subscription;

import java.util.Objects;

/**
 * Проверенные параметры интерактивной сессии подписок.
 *
 * @param workers положительное число платформенных worker-потоков для выполнения запросов
 */
public record SubscriptionConfig(int workers, PoolType pool) {
    /**
     * Не позволяет начать сессию без worker-потоков для выполнения запросов.
     */
    public SubscriptionConfig {
        if (workers <= 0) {
            throw new IllegalArgumentException("Число worker-потоков должно быть положительным");
        }
        Objects.requireNonNull(pool, "Тип пула обязателен");
    }

    public SubscriptionConfig(int workers) {
        this(workers, PoolType.QUEUE);
    }
}
