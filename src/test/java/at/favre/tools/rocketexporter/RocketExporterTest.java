package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
import at.favre.tools.rocketexporter.dto.*;
import at.favre.tools.rocketexporter.model.Message;
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
import static junit.framework.TestCase.*;

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
                        .withBodyFile("mock/example_login.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/groups.list"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock/example_groups.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/groups.history"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock/example_group_history.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/channels.list"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock/example_channels.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/channels.history"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock/example_channel_history.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/im.list"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock/example_dms.json")));

        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/im.history"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("mock/example_dm_history.json")));
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
    public void listGroups() throws Exception {
        login();
        List<RocketChatGroups.Group> groups = exporter.listGroups();
        assertNotNull(groups);
        assertEquals(3, groups.size());
    }

    @Test
    public void listChannels() throws Exception {
        login();
        List<RocketChatChannel.Channel> channels = exporter.listChannels();
        assertNotNull(channels);
        assertEquals(2, channels.size());
    }

    @Test
    public void listDms() throws Exception {
        login();
        List<RocketChatDm.DirectMessage> dm = exporter.listDirectMessageChannels();
        assertNotNull(dm);
        assertEquals(2, dm.size());
    }


    @Test
    public void exportPrivateGroupMessages() throws Exception {
        login();
        File tempFile = testFolder.newFile("out-test-group.csv");
        List<Message> msg = exporter.exportPrivateGroupMessages("roomName", "roomId", 0, 2000, tempFile, new SlackCsvFormat());
        assertEquals(48, msg.size());
        assertTrue(tempFile.exists() && tempFile.isFile() && tempFile.length() > 0);
    }

    @Test
    public void exportChannelMessages() throws Exception {
        login();
        File tempFile = testFolder.newFile("out-test-channel.csv");
        List<Message> msg = exporter.exportChannelMessages("roomName", "roomId", 0, 2000, tempFile, new SlackCsvFormat());
        assertEquals(3, msg.size());
        assertTrue(tempFile.exists() && tempFile.isFile() && tempFile.length() > 0);
    }

    @Test
    public void exportDms() throws Exception {
        login();
        File tempFile = testFolder.newFile("out-test-dm.csv");
        List<Message> msg = exporter.exportDirectMessages("roomName", "roomId", 0, 2000, tempFile, new SlackCsvFormat());
        assertEquals(2, msg.size());
        assertTrue(tempFile.exists() && tempFile.isFile() && tempFile.length() > 0);
    }
}
