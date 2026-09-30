package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.queue.RedisQueueService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Test {

    private final RedisQueueService redisQueueService;

    public Test(RedisQueueService redisQueueService) {
        this.redisQueueService = redisQueueService;
    }

    @GetMapping("/redisQueueService")
    public String getQueue() {

        redisQueueService.enqueue(105L);
        redisQueueService.enqueue(106L);
        redisQueueService.enqueue(107L);

        return "Jobs added to Redis queue";
    }

    @DeleteMapping("/redisQueueService")
    public String clearQueue() {
        redisQueueService.clearQueue();
        return "Queue cleared";
    }
}
