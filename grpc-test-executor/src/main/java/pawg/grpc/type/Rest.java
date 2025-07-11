package pawg.grpc.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class Rest extends AbstractBase {
    protected final HttpClient httpClient;
    protected final URI restUri;
    private final List<StatisticDTO> statisticDTOS;
    protected Gson gson;
    private final TypeToken<List<StatisticDTO>> typeToken =new TypeToken<>() {};

    public Rest(HttpClient httpClient, int numberOfCalls, int numberOfRecords, String username, URI restUri) {
        super(numberOfCalls, numberOfRecords, username);
        this.httpClient = httpClient;
        this.restUri = restUri;
        this.statisticDTOS = Stream.generate(() -> new StatisticDTO(UUID.randomUUID().toString(), username)).limit(numberOfRecords).toList();
        this.gson = new GsonBuilder().create();
    }

    @Override
    protected void execute(int requestNumber) {
        String payload = gson.toJson(statisticDTOS);
        HttpRequest post = HttpRequest.newBuilder()
                                      .uri(restUri)
                                      .header("Content-Type", "application/json")
                                      .header("Accept", "application/json")
                                      .POST(HttpRequest.BodyPublishers.ofString(payload))
                                      .build();
        try {
            HttpResponse<String> response = httpClient.send(post, BodyHandlers.ofString(StandardCharsets.UTF_8));
            List<StatisticDTO> statistics = gson.fromJson(response.body(), typeToken);

            if (requestNumber % 100 == 0) {
                responsePayloadSize = response.body().getBytes().length;
                System.out.printf("%d. REST call completed. Number of records in payload [%d]. Payload size [%d]. Response payload size [%d].%n",
                        requestNumber, statistics.size(), payload.getBytes().length, responsePayloadSize);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public int requestPayloadSize() {
        return gson.toJson(statisticDTOS).getBytes().length;
    }

    @Override
    public int responsePayloadSize() {
        return responsePayloadSize;
    }

}
