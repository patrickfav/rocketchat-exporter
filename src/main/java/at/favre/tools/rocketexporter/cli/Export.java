package at.favre.tools.rocketexporter.cli;

import at.favre.tools.rocketexporter.Config;
import at.favre.tools.rocketexporter.RocketExporter;
import at.favre.tools.rocketexporter.TooManyRequestException;
import at.favre.tools.rocketexporter.converter.ExportFormat;
import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
import at.favre.tools.rocketexporter.dto.Conversation;
import at.favre.tools.rocketexporter.dto.LoginDto;
import at.favre.tools.rocketexporter.dto.LoginResponseDto;
import at.favre.tools.rocketexporter.dto.TokenDto;
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

    @CommandLine.Option(names = {"-o", "--outFile"}, description = "The file or directory to write the export data to. Will write to current directory with auto generated filename if this arg is omitted. If you want to export multiple conversations you must pass a directory not a file.")
    private File file;

    @CommandLine.Option(names = {"-t", "--host"}, required = true, description = "The rocket chat server. E.g. 'https://myserver.com'")
    private URL host;

    @CommandLine.Option(names = {"-u", "--user"}, required = false, description = "RocketChat username for authentication.")
    private String username;

    @CommandLine.Option(names = {"-k", "--user-id"}, required = true, description = "RocketChat Personal Access Token user ID.")
    private String userId;

    @CommandLine.Option(names = {"--debug"}, description = "Add debug log output to STDOUT.")
    private boolean debug;

    @CommandLine.Option(names = {"-m", "--maxMsg"}, description = "How many messages should be exported.")
    private int maxMessages = 25000;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Export())
                .setCaseInsensitiveEnumValuesAllowed(true).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        PrintStream out = System.out;

        if (username == null && userId == null) {
            out.println("You have to use a username or a token user ID to continue.");
            System.exit(-1);
        }

        out.println("Please enter your RocketChat password or token:");

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

            LoginResponseDto loginResponse;
            if (username != null && !username.isEmpty()) {
                loginResponse = exporter.login(new LoginDto(username, password));
            } else {
                loginResponse = exporter.tokenAuth(new TokenDto(userId, password));
            }

            out.println("Authentication successful (" + username + " or " + userId + ").");

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

            List<Conversation> conversationSelection = new ArrayList<>();
            conversationSelection.add(new Conversation.AllConversations());

            List<Conversation> allConversations = conversations.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Conversation::getName))
                    .collect(Collectors.toList());

            conversationSelection.addAll(allConversations);

            if (allConversations.size() == 0) {
                out.println("Nothing found to export.");
                return;
            }

            CliOptionChooser cliOptionChooser =
                    new CliOptionChooser(System.in, out,
                            conversationSelection.stream().map(Conversation::getName).collect(Collectors.toList()),
                            "\nPlease choose the " + type.name + " you want to export:");

            int selection = cliOptionChooser.prompt();
            List<Conversation> toExport = new ArrayList<>();

            if (selection == 0) {
                toExport.addAll(allConversations);
            } else {
                toExport.add(allConversations.get(selection));
            }

            for (int i = 0; i < toExport.size(); i++) {
                Conversation selectedGroup = toExport.get(i);

                final List<Message> messages;
                final ExportFormat format = new SlackCsvFormat();
                final int offset = 0;
                final int maxMsg = maxMessages;
                final File outFile = generateOutputFile(file, selectedGroup.getName(), type, format);

                try {
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
                } catch (TooManyRequestException e) {
                    out.println("Too many requests. Slowing down...");
                    Thread.sleep(5000);
                    i--;
                }
            }
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
