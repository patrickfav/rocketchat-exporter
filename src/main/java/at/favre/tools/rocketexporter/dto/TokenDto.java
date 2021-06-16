package at.favre.tools.rocketexporter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Personal Access Token data.
 * <p>
 * See: https://developer.rocket.chat/api/rest-api/personal-access-tokens
 */
@Data
@AllArgsConstructor
public class TokenDto {
    private String userId;
    private String token;
}
