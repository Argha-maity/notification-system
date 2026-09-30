package argha.example.notification_processing_system.worker;

import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.service.JobProcessingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class NotificationWorker {
    private final RedisQueueService redisQueueService;
    private final JobProcessingService jobProcessingService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5,
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "notification-worker-" + count.incrementAndGet());
                        t.setDaemon(false);
                        return t;
                    }
                }
            );


    public NotificationWorker(RedisQueueService redisQueueService, JobProcessingService jobProcessingService){
        this.redisQueueService = redisQueueService;
        this.jobProcessingService = jobProcessingService;
    }

    @PostConstruct
    public void startWorker(){
        for(int i=0;i<5;i++){
            executorService.submit(this::processQueue);
        }
    }

    //this is for single thread
//    @PostConstruct
//    public void startWorker(){
//        Thread workerThread=new Thread(()->{
//            while(true){
//                try{
////                    System.out.println("Starting worker thread");
//                    Long jobId= redisQueueService.dequeue();
////                    System.out.println("Worker take the jobId: " + jobId);
//                    if(jobId!=null){
//                        jobProcessingService.processJob(jobId);
////                        System.out.println("Job"+jobId+" processed completed");
//                    }
//
//                    Thread.sleep(100);
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        workerThread.start();
//    }

    private void processQueue(){
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Long jobId = redisQueueService.dequeue();
                if(jobId != null) {
                    jobProcessingService.processJob(jobId);
                } else {
                    // Sleep 100ms if queue is empty to avoid busy-wait
                    Thread.sleep(100);
                }
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch(Exception e) {
                log.error("Error processing job", e);
                try {
                    Thread.sleep(1000);  // Back off on error
                } catch(InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch(InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
