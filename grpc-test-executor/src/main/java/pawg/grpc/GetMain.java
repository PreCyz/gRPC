package pawg.grpc;

import com.google.gson.Gson;
import pawg.grpc.service.statistics.StatisticResponse;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class GetMain {

//    private static final String HOST = "Grpc-vs-REST.eu-north-1.elasticbeanstalk.com";
    private static final String HOST = "localhost";
    private static final String USERNAME = "PAWG";
    private static final int GRPC_PORT = 9090;
    private static final int REST_PORT = 8080;
    private static final URI REST_URI = URI.create("http://%s:%d/statistics/%s".formatted(HOST, REST_PORT, USERNAME));
    private static final URI REST_PROTOBUF_URI = URI.create("http://%s:%d/statistics/protobuf/%s".formatted(HOST, REST_PORT, USERNAME));
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        var start = LocalDateTime.now();

        var executor = Executors.newFixedThreadPool(3);
        int numberOfCalls = 1000;
        var grpcClient = new GrpcClient(HOST, GRPC_PORT);

        try (var restClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
             var restProtobufClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {

            CompletableFuture.allOf(
                    CompletableFuture.runAsync(runnableGrpc(grpcClient, numberOfCalls), executor),
                    CompletableFuture.runAsync(runnableRest(restClient, numberOfCalls), executor),
                    CompletableFuture.runAsync(runnableRestProtobuf(restProtobufClient, numberOfCalls), executor)
            ).join();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            grpcClient.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        } finally {
            executor.shutdown();
            Duration duration = Duration.between(start, LocalDateTime.now());
            System.out.printf("Test completed in %d:%d.%n", duration.toMinutes(), duration.toSecondsPart());
        }
        System.exit(0);
    }

    private static Runnable runnableGrpc(final GrpcClient grpcClient, final int numberOfRequests) {
        return () -> {
            for (int i = 1; i <= numberOfRequests; i++) {
                LocalDateTime start = LocalDateTime.now();
                executeGrpcCall(grpcClient, i);
                Duration duration = Duration.between(start, LocalDateTime.now());

                writeResultToFile("grpc.csv", duration);
            }
        };
    }

    private static void executeGrpcCall(GrpcClient client, int counter) {
        StatisticResponse statisticResponse = client.fetchStatistic(USERNAME);
        System.out.printf("%d. gRPC call completed [%s].%n", counter, statisticResponse.getUsername());
    }

    private static void writeResultToFile(String fileName, Duration duration) {
        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            fileWriter.append(String.valueOf(duration.toMillis())).append("\n");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static Runnable runnableRest(final HttpClient httpClient, final int numberOfRequests) {
        return () -> {
            for (int i = 1; i <= numberOfRequests; i++) {
                LocalDateTime start = LocalDateTime.now();
                executeRestCall(httpClient, i);
                Duration duration = Duration.between(start, LocalDateTime.now());

                writeResultToFile("rest.csv", duration);
            }
        };
    }

    private static void executeRestCall(final HttpClient httpClient, final int counter) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(REST_URI)
                                             .GET()
                                             .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            StatisticDTO statisticDTO = GSON.fromJson(response.body(), StatisticDTO.class);
            System.out.printf("%d. REST call completed [%s].%n", counter, statisticDTO.username);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private static Runnable runnableRestProtobuf(HttpClient httpClient, int numberOfRequests) {
        return () -> {
            for (int i = 1; i <= numberOfRequests; i++) {
                LocalDateTime start = LocalDateTime.now();
                executeRestProtobufCall(httpClient, i);
                Duration duration = Duration.between(start, LocalDateTime.now());

                writeResultToFile("protobuf.csv", duration);
            }
        };
    }

    private static void executeRestProtobufCall(final HttpClient client, final int counter) {
        try {
            HttpRequest get = HttpRequest.newBuilder()
                    .GET()
                    .uri(REST_PROTOBUF_URI)
                    .header("Content-Type", "application/x-protobuf")
                    .header("Accept", "application/x-protobuf")
                    .build();

            try (InputStream stream = client.send(get, BodyHandlers.ofInputStream()).body()) {
                StatisticResponse statisticResponse = StatisticResponse.parseFrom(stream);
                System.out.printf("%d. Protobuf call completed [%s].%n", counter, statisticResponse.getUsername());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

}