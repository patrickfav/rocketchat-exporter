package at.favre.tools.rocketexporter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Personal Access Token data.
 * <p>
 * See: <a href="https://developer.rocket.chat/api/rest-api/personal-access-tokens">personal-access-tokens</a>
 */
@Data
@AllArgsConstructor
public class TokenDto {
    private String userId;
    private String token;
}
