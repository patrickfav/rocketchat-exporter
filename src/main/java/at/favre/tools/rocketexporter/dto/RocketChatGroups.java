package at.favre.tools.rocketexporter.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * Response list of groups accessible to a user.
 * <p>
 * See: <a href="https://rocket.chat/docs/developer-guides/rest-api/groups/list/">list</a>
 * <p>
 * Example:
 * <pre>
 * {
 *     "groups": [
 *         {
 *             "_id": "ABABABABABAABABA",
 *             "name": "airport-random",
 *             "fname": "airport-random",
 *             "t": "p",
 *             "msgs": 8,
 *             "usersCount": 3,
 *             "u": {
 *                 "_id": "ABABABABABAABABA",
 *                 "username": "patrick.f"
 *             },
 *             "customFields": {},
 *             "broadcast": false,
 *             "encrypted": false,
 *             "ts": "2019-01-18T22:06:23.223Z",
 *             "ro": false,
 *             "sysMes": true,
 *             "_updatedAt": "2019-03-15T00:00:28.685Z",
 *             "topic": "Various things to discuss",
 *             "lm": "2019-01-23T11:30:42.789Z",
 *             "lastMessage": {
 *                 "_id": "ABABABABABAABABA",
 *                 "rid": "ABABABABABAABABA",
 *                 "msg": "Sehr cool, Danke! Finde die Idee mit dem Reading Club gut :)",
 *                 "ts": "2019-01-23T13:30:42.789Z",
 *                 "u": {
 *                     "_id": "ABABABABABAABABA",
 *                     "username": "gg",
 *                     "name": "G G"
 *                 },
 *                 "mentions": [],
 *                 "channels": [],
 *                 "_updatedAt": "2019-02-23T15:30:42.801Z",
 *                 "sandstormSessionId": null
 *             }
 *         }
 *     ],
 *     "offset": 0,
 *     "count": 12,
 *     "total": 12,
 *     "success": true
 * }
 * </pre>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class RocketChatGroups extends Pageable {
    private List<Group> groups;

    @Data
    public static class Group implements Conversation {
        private String _id;
        private String name;
        private String fname;
        private String usersCount;
        private String topic;
        private String msgs;
        private String _updatedAt;
        private String ts;
    }
}
