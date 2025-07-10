package pawg.grpc.client;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import pawg.grpc.service.statistics.StatisticResponse;

public class StatisticProtobufClient {

    public static void main(String[] args) throws Exception {

        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpRequest get = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create("http://localhost:8080/statistics/protobuf/PAWG"))
                                         .header("Content-Type", "application/x-protobuf")
                                         .header("Accept", "application/x-protobuf")
                                         .build();

            try (InputStream stream = client.send(get, BodyHandlers.ofInputStream()).body()) {
                StatisticResponse statisticResponse = StatisticResponse.parseFrom(stream);
                System.out.println(statisticResponse);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}