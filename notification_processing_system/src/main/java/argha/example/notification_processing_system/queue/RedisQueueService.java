package argha.example.notification_processing_system.queue;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
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

        System.out.println("Enqueued jobId: " + jobId);
    }

    public Long dequeue() {
        return redisTemplate.opsForList().leftPop(
                                        QueueConstants.NOTIFICATION_QUEUE
                                     );
    }

    public void clearQueue() {
        redisTemplate.delete("notification:queue");
    }
}
