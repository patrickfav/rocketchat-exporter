package at.favre.tools.rocketexporter;

import java.util.SortedSet;
import java.util.TreeSet;

import at.favre.tools.rocketexporter.model.Message;
import lombok.Getter;

public class TooManyRequestException extends Exception {
    TooManyRequestException(Object body) {
        super(body != null ? body.toString() : "");
        offset = 0;
        messages = new TreeSet<>();
    }
    
    @Getter
    private final int offset;
    @Getter
    private final SortedSet<Message> messages;
    TooManyRequestException(Object body, int offset, SortedSet<Message> messages) {
        super(body != null ? body.toString() : "");
        this.offset = offset;
        this.messages = messages;
    }
}
