package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
import at.favre.tools.rocketexporter.dto.LoginDto;
import at.favre.tools.rocketexporter.dto.LoginResponseDto;
import at.favre.tools.rocketexporter.dto.RocketChatGroups;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class RocketExporterTest {
    private static final int PORT = 3001;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private RocketExporter exporter;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT));

    @Before
    public void setup() {
        setupMockServer();

        exporter = RocketExporter.newInstance(
                Config.builder()
                        .httpDebugOutput(true)
                        .host(URI.create("http://localhost:" + PORT)).build()
        );
    }

    private void setupMockServer() {
        wireMockRule.stubFor(get(urlPathEqualTo("/test")).willReturn(ok("works")));

        wireMockRule.stubFor(post(urlPathEqualTo("/api/v1/login"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("/mock/example_login.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/groups.list"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("/mock/example_groups.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/groups.history"))
                .willReturn(ok()
                        .withHeader("Content-Type", "api/v1/groups.history")
                        .withBodyFile("/mock/example_messages_history.json")));
    }

    @Test
    public void login() throws Exception {
        LoginResponseDto dto = exporter.login(new LoginDto("user", "password"));
        assertNotNull(dto);
        assertNotNull(dto.getData());
        assertNotNull(dto.getData().getUserId());
        assertNotNull(dto.getData().getAuthToken());
    }

    @Test
    public void listChannels() throws Exception {
        login();
        List<RocketChatGroups.Group> groups = exporter.listChannels();
        assertNotNull(groups);
        assertEquals(3, groups.size());
    }

    @Test
    public void exportPrivateGroupMessages() throws Exception {
        login();
        File tempFile = testFolder.newFile("out-test.csv");

        exporter.exportPrivateGroupMessages("roomName", "roomId", 0, 2000, tempFile, new SlackCsvFormat());
    }

}
