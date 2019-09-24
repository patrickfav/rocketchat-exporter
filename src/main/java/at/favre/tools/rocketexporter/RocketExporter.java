package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.converter.ExportFormat;
import at.favre.tools.rocketexporter.converter.SlackCsvFormat;
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
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RocketExporter {

    public static void main(String[] args) {
        try {
            new RocketExporter(Config.builder()
                    .host(URI.create("url"))
                    .httpDebugOutput(false)
                    .msDelayBetweenRequests(2000)
                    .messagesCountPerRequest(50)
                    .build())
                    .login(new LoginDto("USER", "PW"))
                    .exportPrivateGroupMessages(
                            "mobile-random", "GROUP_MOBILE_RANDOM",
                            300, 400,
                            new SlackCsvFormat());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Config config;

    private RocketChatService service;
    private Map<String, String> authHeaders;

    protected RocketExporter(Config config) {
        this.config = config;
    }

    public RocketExporter login(LoginDto login) throws IOException {
        LoginResponseDto loginResponse = getService().login(login).execute().body();
        authHeaders = Map.of(
                "X-User-Id", loginResponse.getData().getUserId(),
                "X-Auth-Token", loginResponse.getData().getAuthToken());
        return this;
    }

    public List<RocketChatGroups.Group> listChannels() throws IOException {
        RocketChatGroups groups = getService().getAllGroups(authHeaders).execute().body();
        return groups.getGroups();
    }

    public RocketExporter exportPrivateGroupMessages(String roomName, String roomId,
                                                     int offset, int maxMessageCount,
                                                     ExportFormat exportFormat) throws IOException {
        checkAuthenticated();

        final int msgPerRequest = config.getMessagesCountPerRequest();

        Map<Long, Message> normalizedMessages = new HashMap<>();
        for (int i = offset; i <= maxMessageCount; i += msgPerRequest) {
            Response<RocketChatMessageWrapperDto> response =
                    getService().getAllMessagesFromGroup(authHeaders, roomId, i, msgPerRequest).execute();
            RocketChatMessageWrapperDto messagesBody;

            if (response.code() == 429) {
                //got too many requests error, retry
                i -= msgPerRequest;
            } else if (response.code() == 200 && (messagesBody = response.body()) != null) {
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

            try {
                Thread.sleep(config.getMsDelayBetweenRequests());
            } catch (InterruptedException e) {
                throw new IllegalStateException("sleep was interrupted", e);
            }
        }

        List<Message> normalizedMessagesList = new ArrayList<>(normalizedMessages.values());
        normalizedMessagesList.sort(Comparator.comparingLong(m -> m.getTimestamp().toEpochMilli()));

        System.out.println("Fetched " + normalizedMessagesList.size() + " messages for " + roomName);

        exportFormat.export(
                normalizedMessagesList,
                new FileOutputStream(new File("C:\\rocket-export.csv")));

        return this;
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
