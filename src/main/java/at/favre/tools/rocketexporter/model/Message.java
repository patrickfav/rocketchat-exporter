package at.favre.tools.rocketexporter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class Message implements Comparable<Message> {
    private final String _id;
    private final String message;
    private final String username;
    private final String channel;
    private final Instant timestamp;

    @Override
    public int compareTo(Message o) {
        if (this.equals(o)) {
            return 0;
        }
        return Long.valueOf(timestamp.toEpochMilli()).compareTo(o.getTimestamp().toEpochMilli());
    }
}

