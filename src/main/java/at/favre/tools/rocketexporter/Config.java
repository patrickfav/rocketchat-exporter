package at.favre.tools.rocketexporter;

import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Data
@Builder
public class Config {
    private final URI host;
    private final boolean httpDebugOutput;
}
