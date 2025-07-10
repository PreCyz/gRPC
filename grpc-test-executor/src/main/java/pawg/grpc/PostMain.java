package pawg.grpc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import pawg.grpc.service.statistics.RequestCollection;
import pawg.grpc.service.statistics.ResponseCollection;

public class PostMain {

    //    private static final String HOST = "Grpc-vs-REST.eu-north-1.elasticbeanstalk.com";
    private static final int GRPC_PORT = 9090;
    private static final int REST_PORT = 8080;
    private static final String HOST = "localhost";
    private static final String USERNAME = "PAWG";
    private static final URI REST_URI = URI.create("http://%s:%d/statistics".formatted(HOST, REST_PORT));
    private static final URI REST_PROTOBUF_URI = URI.create("http://%s:%d/statistics/protobuf".formatted(HOST, REST_PORT));
    private static final int NUMBER_OF_RECORDS = 4050;
    private static final int NUMBER_OF_CALLS = 100;
    private static final Gson GSON = new Gson();

    private static final List<Duration> restMillis = new ArrayList<>(NUMBER_OF_CALLS);
    private static final List<Duration> protMillis = new ArrayList<>(NUMBER_OF_CALLS);
    private static final List<Duration> grpcMillis = new ArrayList<>(NUMBER_OF_CALLS);
    private static final RequestCollection REQUEST_COLLECTION = GrpcClient.buildRequestCollection(NUMBER_OF_RECORDS, USERNAME);
    private static final List<StatisticDTO> STATISTIC_DTOS = Stream.generate(() -> new StatisticDTO(UUID.randomUUID().toString(), USERNAME))
                                                                   .limit(NUMBER_OF_RECORDS).toList();

    public static void main(String[] args) {
        var start = LocalDateTime.now();
        var executor = Executors.newFixedThreadPool(3);

        try (var restClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                var restProtobufClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                var grpcClient = new GrpcClient(HOST, GRPC_PORT)) {

            CompletableFuture.allOf(
                                     CompletableFuture.runAsync(runnableGrpc(grpcClient), executor),
                                     CompletableFuture.runAsync(runnableRest(restClient), executor),
                                     CompletableFuture.runAsync(runnableProtobuf(restProtobufClient), executor)
                             )
                             .whenComplete((r, t) -> {
                                 List<Metric> metrics = buildMetrics();
                                 writeResultToFile(metrics);
                                 System.out.println("=================================");
                                 double restAvg = printAndGetAvg(restMillis, "rest");
                                 double protAvg = printAndGetAvg(protMillis, "prot");
                                 double grpcAvg = printAndGetAvg(grpcMillis, "grpc");
                                 System.out.printf("Prot gain = %.2f%s%n", (100 * (restAvg - protAvg) / restAvg), "%");
                                 System.out.printf("gRPC gain = %.2f%s%n", (100 * (restAvg - grpcAvg) / restAvg), "%");
                             })
                             .join();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            executor.shutdown();
            Duration duration = Duration.between(start, LocalDateTime.now());
            if (duration.toMinutesPart() > 0) {
                System.out.printf("Test completed in %dm:%ds.%n", duration.toMinutes(), duration.toSecondsPart());
            } else {
                System.out.printf("Test completed in %ds.%n", duration.toSecondsPart());
            }
        }
        System.exit(0);
    }

    private static Runnable runnableGrpc(final GrpcClient grpcClient) {
        return () -> {
            for (int i = 1; i <= NUMBER_OF_CALLS; i++) {
                LocalDateTime start = LocalDateTime.now();
                executeGrpcCall(grpcClient, i);
                grpcMillis.add(Duration.between(start, LocalDateTime.now()));
            }
        };
    }

    private static void executeGrpcCall(GrpcClient client, int counter) {
        ResponseCollection response = client.fetchStatistics(REQUEST_COLLECTION);
        System.out.printf("%d. gRPC call completed [%d].%n", counter, response.getStatisticsCount());
    }

    private static void writeResultToFile(List<Metric> metrics) {
        Path resultCSV = Paths.get("result.csv");
        try {
            Files.deleteIfExists(resultCSV);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        try (FileWriter fileWriter = new FileWriter(resultCSV.toFile(), true)) {
            fileWriter.append(Metric.csvHeaders()).append("\n");
            int idx = 0;
            for (Metric metric : metrics) {
                fileWriter.append(String.valueOf(metric.restMillis()))
                          .append(";")
                          .append(String.valueOf(metric.restProtoMillis()))
                          .append(";").append(String.valueOf(metric.grpcMillis()))
                          .append(";").append(idx++ % 100 == 0 ? "" : String.valueOf(idx))
                          .append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static List<Metric> buildMetrics() {
        List<Metric> metics = new ArrayList<>(NUMBER_OF_CALLS);
        for (int i = 0; i < NUMBER_OF_CALLS; i++) {
            metics.add(new Metric(
                    restMillis.get(i).toMillis(),
                    protMillis.get(i).toMillis(),
                    grpcMillis.get(i).toMillis()

            ));
        }
        return metics;
    }

    private static Runnable runnableRest(final HttpClient httpClient) {
        return () -> {
            for (int i = 1; i <= NUMBER_OF_CALLS; i++) {
                LocalDateTime start = LocalDateTime.now();
                executeRestCall(httpClient, i);
                restMillis.add(Duration.between(start, LocalDateTime.now()));
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
            TypeToken<List<StatisticDTO>> typeToken = new TypeToken<>() {};
            List<StatisticDTO> statistics = GSON.fromJson(response.body(), typeToken);
            System.out.printf("%d. REST call completed [%s].%n", counter, statistics.size());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private static Runnable runnableProtobuf(HttpClient httpClient) {
        return () -> {
            for (int i = 1; i <= NUMBER_OF_CALLS; i++) {
                LocalDateTime start = LocalDateTime.now();
                executeRestProtobufCall(httpClient, i);
                protMillis.add(Duration.between(start, LocalDateTime.now()));
            }
        };
    }

    private static void executeRestProtobufCall(final HttpClient client, final int counter) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            REQUEST_COLLECTION.writeTo(out);
            try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {

                HttpRequest post = HttpRequest.newBuilder()
                                              .POST(HttpRequest.BodyPublishers.ofInputStream(() -> in))
                                              .uri(REST_PROTOBUF_URI)
                                              .header("Content-Type", "application/x-protobuf")
                                              .header("Accept", "application/x-protobuf")
                                              .build();

                try (InputStream is = client.send(post, BodyHandlers.ofInputStream()).body()) {
                    ResponseCollection response = ResponseCollection.parseFrom(is);
                    System.out.printf("%d. Protobuf call completed [%s].%n", counter, response.getStatisticsCount());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static double printAndGetAvg(List<Duration> durations, String name) {
        double avg = (double) durations.stream().mapToLong(Duration::toMillis).sum() / NUMBER_OF_CALLS;
        System.out.printf("AVG(%s) = %.3f%n", name, avg);
        return avg;
    }

}