package at.favre.tools.rocketexporter.cli;

import at.favre.tools.rocketexporter.Config;
import at.favre.tools.rocketexporter.RocketExporter;
import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
import at.favre.tools.rocketexporter.dto.LoginDto;
import at.favre.tools.rocketexporter.dto.LoginResponseDto;
import at.favre.tools.rocketexporter.dto.RocketChatGroups;
import at.favre.tools.rocketexporter.model.Message;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@CommandLine.Command(description = "Exports rocket chat messages from a specific group/channel.",
        name = "export", mixinStandardHelpOptions = true, version = "1.0")
class Export implements Runnable {

    @CommandLine.Option(names = {"-o", "--outFile"}, required = true, description = "The file to write the export to")
    private File file;

    @CommandLine.Option(names = {"-t", "--host"}, required = true, description = "The rocket chat server. E.g. 'https://myserver.com'")
    private URL host;

    @CommandLine.Option(names = {"-u", "--user"}, required = true, description = "RocketChat username for authentication.")
    private String username;

    @CommandLine.Option(names = {"--debug"}, description = "Add debug log output to STDOUT.")
    private boolean debug;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Export())
                .setCaseInsensitiveEnumValuesAllowed(true).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        PrintStream out = System.out;

        out.println("Please enter your RocketChat password:");

        String password;
        if (System.console() != null) {
            password = String.valueOf(System.console().readPassword());
        } else {
            password = new Scanner(System.in).next();
        }

        try {
            RocketExporter exporter = RocketExporter.newInstance(
                    Config.builder()
                            .host(host.toURI())
                            .httpDebugOutput(debug)
                            .build());

            LoginResponseDto loginResponse = exporter.login(new LoginDto(username, password));

            out.println("Authentication successful (" + loginResponse.getData().getUserId() + ").");

            List<RocketChatGroups.Group> groups = exporter.listChannels().stream()
                    .sorted(Comparator.comparing(RocketChatGroups.Group::getName))
                    .collect(Collectors.toList());

            if (groups.size() == 0) {
                out.println("No groups found to export.");
                return;
            }

            CliOptionChooser cliOptionChooser =
                    new CliOptionChooser(System.in, out,
                            groups.stream().map(RocketChatGroups.Group::getName).collect(Collectors.toList()),
                            "\nPlease choose to channel you want to export:");

            RocketChatGroups.Group selectedGroup = groups.get(cliOptionChooser.prompt());

            List<Message> messages = exporter.exportPrivateGroupMessages(selectedGroup.getName(), selectedGroup.get_id(), 0, 10000, file, new SlackCsvFormat());

            out.println("Successfully exported " + messages.size() + " messages to '" + file + "'");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
