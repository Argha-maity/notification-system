package argha.example.notification_processing_system.queue;

public class QueueConstants {
    private QueueConstants() {}

    public static final String NOTIFICATION_QUEUE = "notification:queue";
    public static final String DEAD_LETTER_QUEUE = "notification:dead_letter_queue";
}