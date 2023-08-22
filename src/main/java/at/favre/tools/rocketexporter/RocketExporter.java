package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.converter.ExportFormat;
import at.favre.tools.rocketexporter.dto.*;
import at.favre.tools.rocketexporter.model.Message;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The main interface for exporting rocket chat data.
 */
public interface RocketExporter {
    /**
     * Authenticate with the server for all actions that need login.
     *
     * @param login credentials
     * @return response
     * @throws IOException on issues during the REST call
     */
    LoginResponseDto login(LoginDto login) throws IOException;

    /**
     * Use the Personal Access Token for all communications
     *
     * @param token PAT user ID and token
     * @return response
     * @throws IOException on issues during the REST call
     */
    LoginResponseDto tokenAuth(TokenDto token) throws IOException;

    /**
     * Get all accessible groups.
     * Requires login first.
     *
     * @return list of all channels
     * @throws IOException on issues during the REST call
     */
    List<RocketChatGroups.Group> listGroups() throws IOException;

    /**
     * Get all accessible channels.
     * Requires login first.
     *
     * @return list of all channels
     * @throws IOException on issues during the REST call
     */
    List<RocketChatChannel.Channel> listChannels() throws IOException;

    /**
     * Get all direct message conversations.
     * Requires login first.
     *
     * @return list of all channels
     * @throws IOException on issues during the REST call
     */
    List<RocketChatDm.DirectMessage> listDirectMessageChannels() throws IOException;

    /**
     * Export messages from a group.
     * Requires login first.
     *
     * @param roomName        name used in the export
     * @param roomId          used identify the room within the REST Api
     * @param offset          of messages to export (0 means "from the most recent")
     * @param maxMessageCount how many messages to export
     * @param exportFormat    selected output format
     * @return exported messages
     * @throws IOException on issues during the REST call
     * @throws TooManyRequestException if the server responds with 429, you need to throttle the requests
     */
    SortedSet<Message> exportPrivateGroupMessages(SortedSet<Message> messages, String roomName, String roomId,
                                             int offset, int maxMessageCount,
                                             File out, ExportFormat exportFormat) throws IOException, TooManyRequestException;

    /**
     * Export messages from a channel.
     * Requires login first.
     *
     * @param channelName     name used in the export
     * @param channelId       used identify the room within the REST Api
     * @param offset          of messages to export (0 means "from the most recent")
     * @param maxMessageCount how many messages to export
     * @param exportFormat    selected output format
     * @return exported messages
     * @throws IOException on issues during the REST call
     * @throws TooManyRequestException if the server responds with 429, you need to throttle the requests
     */
    SortedSet<Message> exportChannelMessages(SortedSet<Message> messages, String channelName, String channelId,
                                        int offset, int maxMessageCount,
                                        File out, ExportFormat exportFormat) throws IOException, TooManyRequestException;

    /**
     * Export messages from a direct message conversation.
     * Requires login first.
     *
     * @param dmName          name used in the export
     * @param dmId            used identify the room within the REST Api
     * @param offset          of messages to export (0 means "from the most recent")
     * @param maxMessageCount how many messages to export
     * @param exportFormat    selected output format
     * @return exported messages
     * @throws IOException on issues during the REST call
     * @throws TooManyRequestException if the server responds with 429, you need to throttle the requests
     */
    SortedSet<Message> exportDirectMessages(SortedSet<Message> messages, String dmName, String dmId,
                                       int offset, int maxMessageCount,
                                       File out, ExportFormat exportFormat) throws IOException, TooManyRequestException;

    /**
     * Creates a new instance of exporter
     *
     * @param config to set
     * @return new instance
     */
    static RocketExporter newInstance(Config config) {
        return new Default(config);
    }

    /**
     * Type of conversation to export
     */
    enum ConversationType {
        GROUP(0, "group"),
        CHANNEL(1, "channel"),
        DIRECT_MESSAGES(2, "direct message");

        public final int id;
        public final String name;

        ConversationType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static ConversationType of(int id) {
            for (ConversationType value : ConversationType.values()) {
                if (value.id == id) {
                    return value;
                }
            }
            throw new IllegalArgumentException("unknown type");
        }
    }

    /**
     * Default implementation
     */
    class Default implements RocketExporter {
        private final Config config;

        private RocketChatService service;
        private Map<String, String> authHeaders;
        private String userName;

        Default(Config config) {
            this.config = config;
        }

        @Override
        public LoginResponseDto login(LoginDto login) throws IOException {
            Response<LoginResponseDto> loginResponse = getService().login(login).execute();
            LoginResponseDto loginResponseBody;

            if (loginResponse.code() == 401 || loginResponse.code() == 403) {
                throw new IllegalArgumentException("invalid credentials");
            } else if (loginResponse.code() == 200 && (loginResponseBody = loginResponse.body()) != null) {
                userName = loginResponseBody.getData().getMe().getUsername();
                authHeaders = Map.of(
                        "X-User-Id", loginResponseBody.getData().getUserId(),
                        "X-Auth-Token", loginResponseBody.getData().getAuthToken());
                return loginResponseBody;
            } else {
                throw new IllegalStateException("error response: " + loginResponse.code());
            }
        }

        public LoginResponseDto tokenAuth(TokenDto token) throws IOException {
            authHeaders = Map.of(
                    "X-User-Id", token.getUserId(),
                    "X-Auth-Token", token.getToken());

            Response<LoginResponseDto> loginResponse = getService().tokenAuth(authHeaders).execute();
            LoginResponseDto loginResponseBody;

            if (loginResponse.code() == 401 || loginResponse.code() == 403) {
                throw new IllegalArgumentException("invalid credentials");
            } else if (loginResponse.code() == 200 && (loginResponseBody = loginResponse.body()) != null) {
                return loginResponseBody;
            } else {
                throw new IllegalStateException("error response: " + loginResponse.code());
            }
        }

        @Override
        public List<RocketChatGroups.Group> listGroups() throws IOException {
            checkAuthenticated();
            RocketChatGroups groups = getService().getAllGroups(authHeaders).execute().body();
            if (groups != null) {
                return groups.getGroups();
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public List<RocketChatChannel.Channel> listChannels() throws IOException {
            checkAuthenticated();
            RocketChatChannel channel = getService().getAllChannels(authHeaders).execute().body();
            if (channel != null) {
                return channel.getChannels();
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public List<RocketChatDm.DirectMessage> listDirectMessageChannels() throws IOException {
            checkAuthenticated();
            RocketChatDm channel = getService().getAllDirectMessages(authHeaders).execute().body();
            if (channel != null) {
                return channel.getIms()
                        .stream()
                        .peek(dm -> dm.setGeneratedName(dm.getUsernames().stream()
                            .filter(u -> !u.equals(userName))
                            .findFirst()
                            .orElse(userName))) // Fallback to the user's name if we have filtered out all names (conversations with themselves)
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public SortedSet<Message> exportPrivateGroupMessages(SortedSet<Message> messages, String roomName, String roomId,
                                                        int offset, int maxMessageCount,
                                                        File out, ExportFormat exportFormat) throws IOException, TooManyRequestException {
            return exportMessages(messages, roomName, roomId, offset, maxMessageCount, ConversationType.GROUP, out, exportFormat);
        }

        @Override
        public SortedSet<Message> exportChannelMessages(SortedSet<Message> messages, String channelName, String channelId,
                                                   int offset, int maxMessageCount,
                                                   File out, ExportFormat exportFormat) throws IOException, TooManyRequestException {
            return exportMessages(messages, channelName, channelId, offset, maxMessageCount, ConversationType.CHANNEL, out, exportFormat);
        }

        @Override
        public SortedSet<Message> exportDirectMessages(SortedSet<Message> messages, String dmName, String dmId,
                                                  int offset, int maxMessageCount,
                                                  File out, ExportFormat exportFormat) throws IOException, TooManyRequestException {
            return exportMessages(messages, dmName, dmId, offset, maxMessageCount, ConversationType.DIRECT_MESSAGES, out, exportFormat);
        }

        private SortedSet<Message> exportMessages(SortedSet<Message> messages, String contextName, String id,
                                             int offset, int maxMessageCount,
                                             ConversationType conversationType, File out, ExportFormat exportFormat) throws IOException, TooManyRequestException {
            checkAuthenticated();

            do {
                Response<RocketChatMessageWrapperDto> response;
                switch (conversationType) {
                    case GROUP:
                        response = getService().getAllMessagesFromGroup(authHeaders, id, offset, maxMessageCount).execute();
                        break;
                    case CHANNEL:
                        response = getService().getAllMessagesFromChannels(authHeaders, id, offset, maxMessageCount).execute();
                        break;
                    case DIRECT_MESSAGES:
                        response = getService().getAllMessagesFromDirectMessages(authHeaders, id, offset, maxMessageCount).execute();
                        break;
                    default:
                        throw new IllegalStateException();
                }

                Map<Long, Message> normalizedMessages = new HashMap<>();
                RocketChatMessageWrapperDto messagesBody;

                if (response.code() == 200 && (messagesBody = response.body()) != null) {
                    for (RocketChatMessageWrapperDto.Message message : messagesBody.getMessages()) {
                        Instant timestamp = Instant.parse(message.getTs());

                        normalizedMessages.put(timestamp.toEpochMilli(),
                                new Message(
                                        message.get_id(),
                                        message.getMsg(),
                                        message.getU().getName(),
                                        contextName,
                                        timestamp
                                ));
                    }
                    messages.addAll(normalizedMessages.values());
                    if (messagesBody.getMessages().size() < 100 || messages.size() >= maxMessageCount) {
                        break;
                    } else {
                        offset += 100;
                    }
                } else if (response.code() == 429) {
                    throw new TooManyRequestException(response.body(), offset, messages);
                } else {
                    throw new IllegalStateException("error response: " + response.code());
                }
            } while (true);

            exportFormat.export(
                    new ArrayList<>(messages),
                    new FileOutputStream(out));

            return messages;
        }

        private void checkAuthenticated() {
            if (authHeaders == null) {
                throw new IllegalStateException("authentication required, call login first");
            }
        }

        private RocketChatService getService() {
            if (service == null) {
                service = new Retrofit.Builder()
                        .baseUrl(config.getHost().toString())
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(createClient())
                        .build().create(RocketChatService.class);
            }
            return service;
        }

        private OkHttpClient createClient() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS);
            if (config.isHttpDebugOutput()) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                return builder
                        .addNetworkInterceptor(interceptor)
                        .addInterceptor(interceptor)
                        .build();
            } else {
                return builder.build();
            }
        }
    }
}
