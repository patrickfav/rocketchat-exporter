package at.favre.tools.rocketexporter.dto;

import lombok.Data;

/**
 * Response of the login request.
 * <p>
 * See: <a href="https://rocket.chat/docs/developer-guides/rest-api/authentication/login/">login</a>
 */

@Data
public class LoginResponseDto {
    private final String status;
    private final LoginData data;

    @Data
    public static class LoginData {
        private final String userId;
        private final String authToken;
        private final Me me;
    }

    @Data
    public static class Me {
        private final String _id;
        private final String username;
        private final String name;
        private final String email;
        private final String language;
    }
}
