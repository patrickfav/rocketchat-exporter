package at.favre.tools.rocketexporter.cli;

import at.favre.tools.rocketexporter.Config;
import at.favre.tools.rocketexporter.RocketExporter;
import at.favre.tools.rocketexporter.converter.ExportFormat;
import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
import at.favre.tools.rocketexporter.dto.Conversation;
import at.favre.tools.rocketexporter.dto.LoginDto;
import at.favre.tools.rocketexporter.dto.LoginResponseDto;
import at.favre.tools.rocketexporter.model.Message;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CommandLine.Command(description = "Exports rocket chat messages from a specific group/channel.",
        name = "export", mixinStandardHelpOptions = true, version = "1.0")
class Export implements Runnable {

    @CommandLine.Option(names = {"-o", "--outFile"}, description = "The file or directory to write the export data to. Will write to current directory with auto generated filename if this arg is omitted.")
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

            out.println("Authentication successful (" + loginResponse.getData().getMe().getUsername() + ").");

            CliOptionChooser typeChooser =
                    new CliOptionChooser(System.in, out,
                            List.of(
                                    RocketExporter.ConversationType.GROUP.name,
                                    RocketExporter.ConversationType.CHANNEL.name,
                                    RocketExporter.ConversationType.DIRECT_MESSAGES.name),
                            "\nWhat type do you want to export:");

            ArrayList<Conversation> conversations = new ArrayList<>();
            RocketExporter.ConversationType type = RocketExporter.ConversationType.of(typeChooser.prompt());

            switch (type) {
                case GROUP:
                    conversations.addAll(exporter.listGroups());
                    break;
                case CHANNEL:
                    conversations.addAll(exporter.listChannels());
                    break;
                case DIRECT_MESSAGES:
                    conversations.addAll(exporter.listDirectMessageChannels());
                    break;
                default:
                    throw new IllegalStateException();
            }

            List<Conversation> allConversations = conversations.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Conversation::getName))
                    .collect(Collectors.toList());

            if (allConversations.size() == 0) {
                out.println("Nothing found to export.");
                return;
            }

            CliOptionChooser cliOptionChooser =
                    new CliOptionChooser(System.in, out,
                            allConversations.stream().map(Conversation::getName).collect(Collectors.toList()),
                            "\nPlease choose the " + type.name + " you want to export:");

            Conversation selectedGroup = allConversations.get(cliOptionChooser.prompt());

            final List<Message> messages;
            final ExportFormat format = new SlackCsvFormat();
            final int offset = 0;
            final int maxMsg = 25000;
            final File outFile = generateOutputFile(file, selectedGroup.getName(), type, format);

            switch (type) {
                case GROUP:
                    messages = exporter.exportPrivateGroupMessages(selectedGroup.getName(), selectedGroup.get_id(), offset, maxMsg, outFile, format);
                    break;
                case CHANNEL:
                    messages = exporter.exportChannelMessages(selectedGroup.getName(), selectedGroup.get_id(), offset, maxMsg, outFile, format);
                    break;
                case DIRECT_MESSAGES:
                    messages = exporter.exportDirectMessages(selectedGroup.getName(), selectedGroup.get_id(), offset, maxMsg, outFile, format);
                    break;
                default:
                    throw new IllegalStateException();
            }

            out.println("Successfully exported " + messages.size() + " " + type.name + " messages to '" + outFile + "'");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File generateOutputFile(File provided, String contextName, RocketExporter.ConversationType type, ExportFormat format) {
        if (provided == null) {
            provided = new File("./");
        }

        if (!provided.exists()) {
            provided.mkdirs();
        }

        if (provided.isDirectory()) {
            String filename = type.name.replaceAll(" ", "-") + "_" + contextName + "_" + DateTimeFormatter
                    .ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneId.of("UTC"))
                    .format(Instant.now()) + "." + format.fileExtension();
            return new File(provided, filename);
        } else {
            return provided;
        }
    }
}
