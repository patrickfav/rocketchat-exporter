package at.favre.tools.rocketexporter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Rocket Chat Message DTO
 * <p>
 * See: https://rocket.chat/docs/developer-guides/rest-api/groups/messages/
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
    public List<Message> messages;

    @Data
    @AllArgsConstructor
    public static class Message {
        public String _id;
        public String rid;
        public String msg;
        public String ts;
        public User u;

        @Data
        @AllArgsConstructor
        public static class User {
            public String _id;
            public String username;
            public String name;
        }
    }
}