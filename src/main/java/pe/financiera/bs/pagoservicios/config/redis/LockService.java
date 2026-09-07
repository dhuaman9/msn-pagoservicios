package pe.financiera.bs.pagoservicios.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.financiera.framework.pubsub.messaging.ProcessingResult;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

@Component
@Slf4j
public class LockService {

    private final RMapCache<String, ProcessingResult> lockMap;

    @Value("${queue.subscription.deduplication-lock-ttl}")
    private Integer leaseTimeSeconds;

    public LockService(final RMapCache<String, ProcessingResult> lockMap) {
        this.lockMap = lockMap;
    }

    public void lockBy(final String lockKey, final Consumer<TransactionEvent> consumer, final TransactionEvent event) {
        RLock lock = null;

        try {
            lock = lockMap.getLock(lockKey);
            lock.lock(leaseTimeSeconds, TimeUnit.SECONDS);
            consumer.accept(event);
        } catch (Exception e) {
            log.error("Error executing locked method", e);
            throw e;
        } finally {
            Optional.ofNullable(lock).ifPresent(Lock::unlock);
        }
    }
}
