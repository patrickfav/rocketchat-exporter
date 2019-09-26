package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.converter.ExportFormat;
import at.favre.tools.rocketexporter.dto.LoginDto;
import at.favre.tools.rocketexporter.dto.LoginResponseDto;
import at.favre.tools.rocketexporter.dto.RocketChatGroups;
import at.favre.tools.rocketexporter.dto.RocketChatMessageWrapperDto;
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

/**
 * The main interface for exporting rocket chat data.
 */
public interface RocketExporter {
    /**
     * Authenticate with the server for all actions that need login.
     *
     * @param login credentials
     * @return this
     * @throws IOException
     */
    LoginResponseDto login(LoginDto login) throws IOException;

    /**
     * Get all accessible groups/channels. Requires login first.
     *
     * @return list of all channels
     * @throws IOException
     */
    List<RocketChatGroups.Group> listChannels() throws IOException;

    /**
     * Export messages from a group/channel. Requires login first.
     *
     * @param roomName
     * @param roomId
     * @param offset
     * @param maxMessageCount
     * @param exportFormat
     * @return exported messages
     * @throws IOException
     */
    List<Message> exportPrivateGroupMessages(String roomName, String roomId,
                                             int offset, int maxMessageCount,
                                             File out, ExportFormat exportFormat) throws IOException;

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
     * Default implementation
     */
    class Default implements RocketExporter {
        private final Config config;

        private RocketChatService service;
        private Map<String, String> authHeaders;

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
                authHeaders = Map.of(
                        "X-User-Id", loginResponseBody.getData().getUserId(),
                        "X-Auth-Token", loginResponseBody.getData().getAuthToken());
                return loginResponseBody;
            } else {
                throw new IllegalStateException("error response: " + loginResponse.code());
            }
        }

        @Override
        public List<RocketChatGroups.Group> listChannels() throws IOException {
            RocketChatGroups groups = getService().getAllGroups(authHeaders).execute().body();
            if (groups != null) {
                return groups.getGroups();
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public List<Message> exportPrivateGroupMessages(String roomName, String roomId,
                                                        int offset, int maxMessageCount,
                                                        File out, ExportFormat exportFormat) throws IOException {
            checkAuthenticated();


            Map<Long, Message> normalizedMessages = new HashMap<>();
            Response<RocketChatMessageWrapperDto> response =
                    getService().getAllMessagesFromGroup(authHeaders, roomId, offset, maxMessageCount).execute();
            RocketChatMessageWrapperDto messagesBody;

            if (response.code() == 200 && (messagesBody = response.body()) != null) {
                for (RocketChatMessageWrapperDto.Message message : messagesBody.getMessages()) {
                    Instant timestamp = Instant.parse(message.getTs());

                    normalizedMessages.put(timestamp.toEpochMilli(),
                            new Message(
                                    message.getMsg(),
                                    message.getU().getName(),
                                    roomName,
                                    timestamp
                            ));
                }
            } else {
                throw new IllegalStateException("error response: " + response.code());
            }

            List<Message> normalizedMessagesList = new ArrayList<>(normalizedMessages.values());
            normalizedMessagesList.sort(Comparator.comparingLong(m -> m.getTimestamp().toEpochMilli()));

            exportFormat.export(
                    normalizedMessagesList,
                    new FileOutputStream(out));

            return normalizedMessagesList;
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
