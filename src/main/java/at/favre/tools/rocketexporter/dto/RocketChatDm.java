package at.favre.tools.rocketexporter.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Response list of direct message channels accessible to a user.
 * <p>
 * See: <a href="https://rocket.chat/docs/developer-guides/rest-api/im/list/">list</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RocketChatDm extends Pageable {
    private List<DirectMessage> ims;

    @Data
    public static class DirectMessage implements Conversation {
        private String _id;
        private String msgs;
        private String _updatedAt;
        private String ts;
        private List<String> usernames;
        private String topic;
        private String generatedName;

        @Override
        public String getName() {
            return generatedName != null ? generatedName : String.join("-", usernames);
        }
    }
}
