package argha.example.notification_processing_system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    String email;
    String token;
}
