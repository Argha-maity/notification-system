package argha.example.notification_processing_system.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisQueueService {
    private final RedisTemplate<String, Long> redisTemplate;

    public RedisQueueService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueue(Long jobId) {
        redisTemplate.opsForList().rightPush(
                QueueConstants.NOTIFICATION_QUEUE,
                jobId
        );
        log.info("Enqueued jobId: {} into {}", jobId, QueueConstants.NOTIFICATION_QUEUE);
    }

    public Long dequeue() {
        return redisTemplate.opsForList().leftPop(
                QueueConstants.NOTIFICATION_QUEUE
        );
    }

    public void clearQueue() {
        redisTemplate.delete(QueueConstants.NOTIFICATION_QUEUE);
        log.info("Cleared queue: {}", QueueConstants.NOTIFICATION_QUEUE);
    }

    public void enqueueDeadLetter(Long jobId) {
        redisTemplate.opsForList().rightPush(
                QueueConstants.DEAD_LETTER_QUEUE,
                jobId
        );
        log.warn("Enqueued jobId: {} into DLQ: {}", jobId, QueueConstants.DEAD_LETTER_QUEUE);
    }

    public Long dequeueDeadLetter() {
        return redisTemplate.opsForList().leftPop(
                QueueConstants.DEAD_LETTER_QUEUE
        );
    }

    public void clearDeadLetterQueue() {
        redisTemplate.delete(QueueConstants.DEAD_LETTER_QUEUE);
        log.info("Cleared DLQ: {}", QueueConstants.DEAD_LETTER_QUEUE);
    }

    public long getQueueSize() {
        Long size = redisTemplate.opsForList().size(QueueConstants.NOTIFICATION_QUEUE);
        return size != null ? size : 0L;
    }

    public long getDeadLetterQueueSize() {
        Long size = redisTemplate.opsForList().size(QueueConstants.DEAD_LETTER_QUEUE);
        return size != null ? size : 0L;
    }
}
