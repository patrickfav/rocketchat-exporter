package at.favre.tools.rocketexporter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Login data.
 * <p>
 * See: <a href="https://rocket.chat/docs/developer-guides/rest-api/authentication/login/">authentication</a>
 */
@Data
@AllArgsConstructor
public class LoginDto {
    private String user;
    private String password;
}
