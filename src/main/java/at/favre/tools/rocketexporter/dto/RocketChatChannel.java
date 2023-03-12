package at.favre.tools.rocketexporter.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Response list of channel accessible to a user.
 * <p>
 * See: <a href="https://rocket.chat/docs/developer-guides/rest-api/channels/list/">list</a>
 * <p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RocketChatChannel extends Pageable {
    private List<Channel> channels;

    @Data
    public static class Channel implements Conversation {
        private String _id;
        private String name;
        private String msgs;
        private String _updatedAt;
        private String ts;
    }
}
