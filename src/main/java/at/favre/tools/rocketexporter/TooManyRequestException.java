package at.favre.tools.rocketexporter;

public class TooManyRequestException extends Exception {
    TooManyRequestException(Object body) {
        super(body != null ? body.toString() : "");
    }
}
