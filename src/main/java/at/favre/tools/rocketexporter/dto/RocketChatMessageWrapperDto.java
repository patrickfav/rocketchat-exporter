package at.favre.tools.rocketexporter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Rocket Chat Message DTO
 * <p>
 * See: <a href="https://rocket.chat/docs/developer-guides/rest-api/groups/messages/">messages</a>
 * <p>
 * Example:
 *
 * <pre>
 * {
 *     "_id":"1TgcYmd2c2NgQGLqj",
 *         "rid":"P3s1nkNg8TXB9agQE",
 *         "msg":"_... to pass the floppy disk!_",
 *         "ts":"2019-09-23T10:13:07.097Z",
 *         "u":{
 *     "_id":"jA1kKLb6mnTQbE66t",
 *             "username":"befan.sallerx",
 *             "name":"Befan Sallerx"
 * },
 *     "mentions": [],
 *     "channels": [],
 *     "_updatedAt":"2019-04-03T10:13:07.212Z"
 * }
 * </pre>
 */
@Data
@AllArgsConstructor
public class RocketChatMessageWrapperDto {
    private List<Message> messages;

    @Data
    @AllArgsConstructor
    public static class Message {
        private String _id;
        private String rid;
        private String msg;
        private String ts;
        private User u;

        @Data
        @AllArgsConstructor
        public static class User {
            private String _id;
            private String username;
            private String name;
        }
    }
}
