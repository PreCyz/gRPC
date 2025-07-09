package pawg.grpc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pawg.grpc.service.statistics.RequestCollection;
import pawg.grpc.service.statistics.ResponseCollection;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class PostMain {

    //    private static final String HOST = "Grpc-vs-REST.eu-north-1.elasticbeanstalk.com";
    private static final String HOST = "localhost";
    private static final String USERNAME = "PAWG";
    private static final int GRPC_PORT = 9090;
    private static final int REST_PORT = 8080;
    private static final URI REST_URI = URI.create("http://%s:%d/statistics".formatted(HOST, REST_PORT));
    private static final URI REST_PROTOBUF_URI = URI.create("http://%s:%d/statistics/protobuf".formatted(HOST, REST_PORT));
    private static final Gson GSON = new Gson();
    private static final int NUMBER_OF_RECORDS = 4000;
    private static final RequestCollection REQUEST_COLLECTION = GrpcClient.buildRequestCollection(NUMBER_OF_RECORDS);
    private static final List<StatisticDTO> STATISTIC_DTOS = Stream.generate(() -> new StatisticDTO(UUID.randomUUID().toString(), USERNAME))
            .limit(NUMBER_OF_RECORDS).toList();

    private final static String BINARY = "request_collection.bin";
    public static final String PROTOBUF_CSV = "protobuf.csv";
    public static final String GRPC_CSV = "grpc.csv";
    public static final String REST_CSV = "rest.csv";

    public static void main(String[] args) {
        var start = LocalDateTime.now();

        try {
            Files.deleteIfExists(Paths.get(BINARY));
            Files.deleteIfExists(Paths.get(REST_CSV));
            Files.deleteIfExists(Paths.get(GRPC_CSV));
            Files.deleteIfExists(Paths.get(PROTOBUF_CSV));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        try (FileOutputStream fos = new FileOutputStream(BINARY)) {
            REQUEST_COLLECTION.writeTo(fos);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

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

                writeResultToFile(GRPC_CSV, duration);
            }
        };
    }

    private static void executeGrpcCall(GrpcClient client, int counter) {
        ResponseCollection response = client.fetchStatistics(REQUEST_COLLECTION);
        System.out.printf("%d. gRPC call completed [%d].%n", counter, response.getStatisticsCount());
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

                writeResultToFile(REST_CSV, duration);
            }
        };
    }

    private static void executeRestCall(final HttpClient httpClient, final int counter) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(REST_URI)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(STATISTIC_DTOS)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            TypeToken<List<StatisticDTO>> typeToken = new TypeToken<>() {
            };
            List<StatisticDTO> statistics = GSON.fromJson(response.body(), typeToken);
            System.out.printf("%d. REST call completed [%s].%n", counter, statistics.size());

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

                writeResultToFile(PROTOBUF_CSV, duration);
            }
        };
    }

    private static void executeRestProtobufCall(final HttpClient client, final int counter) {
        try (InputStream is = new FileInputStream(BINARY)) {

            HttpRequest post = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> is))
                    .uri(REST_PROTOBUF_URI)
                    .header("Content-Type", "application/x-protobuf")
                    .header("Accept", "application/x-protobuf")
                    .build();

            try (InputStream stream = client.send(post, BodyHandlers.ofInputStream()).body()) {
                ResponseCollection response = ResponseCollection.parseFrom(stream);
                System.out.printf("%d. Protobuf call completed [%s].%n", counter, response.getStatisticsCount());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

}