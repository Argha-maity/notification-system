package argha.example.notification_processing_system.dto.response;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupResponse {
    private String username;

    @Column(unique = true)
    private String email;
}
