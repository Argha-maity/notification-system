package argha.example.notification_processing_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to != null && !to.isBlank() ? to : "goku87880@gmail.com");
            mailMessage.setFrom("harek0221@gmail.com");
            mailMessage.setSubject(subject != null ? subject : "Notification");
            mailMessage.setText(message != null ? message : "");

            mailSender.send(mailMessage);
            log.info("Email sent successfully to {}", to);
        } catch (MailException err) {
            log.error("Failed to send email to {}: {}", to, err.getMessage());
            throw err;
        }
    }

    public void sendEmail(String subject, String message) {
        sendEmail("goku87880@gmail.com", subject, message);
    }
}
