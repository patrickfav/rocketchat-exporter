package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
import at.favre.tools.rocketexporter.dto.LoginDto;

import java.net.URI;

public final class Start {

    public void main(String[] args) {
        try {
            RocketExporter.newInstance(
                    Config.builder()
                            .host(URI.create("url"))
                            .httpDebugOutput(false)
                            .build())
                    .login(new LoginDto("USER", "PW"))
                    .exportPrivateGroupMessages(
                            "mobile-random", "GROUP_MOBILE_RANDOM",
                            0, 2000,
                            new SlackCsvFormat());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
