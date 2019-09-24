package at.favre.tools.rocketexporter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class Message {
    private final String message;
    private final String username;
    private final String channel;
    private final Instant timestamp;
}

