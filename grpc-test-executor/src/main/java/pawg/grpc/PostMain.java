package pawg.grpc;

import pawg.grpc.type.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class PostMain {

        private static final String HOST = "grpc.eu-north-1.elasticbeanstalk.com";
    private static final int GRPC_PORT = 9090;
    private static final int REST_PORT = 8080;
//    private static final String HOST = "localhost";
    private static final String USERNAME = "PAWG";
    private static final URI REST_URI = URI.create("http://%s:%d/statistics".formatted(HOST, REST_PORT));
    private static final URI REST_PROTOBUF_URI = URI.create("http://%s:%d/statistics/protobuf".formatted(HOST, REST_PORT));
    private static final int NUMBER_OF_RECORDS = 4050;
    private static final int NUMBER_OF_CALLS = 1000;

    public static void main(String[] args) {
        var start = LocalDateTime.now();
        var executor = Executors.newFixedThreadPool(3);

        try (var restClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                var protoClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                var grpcClient = new GrpcClient(HOST, GRPC_PORT)) {

            final Grpc grpc = new Grpc(grpcClient, NUMBER_OF_CALLS, NUMBER_OF_RECORDS, USERNAME);
            final Rest rest = new Rest(restClient, NUMBER_OF_CALLS, NUMBER_OF_RECORDS, USERNAME, REST_URI);
            final RestProtobuf restProtobuf = new RestProtobuf(protoClient, NUMBER_OF_CALLS, NUMBER_OF_RECORDS, USERNAME, REST_PROTOBUF_URI);

            CompletableFuture.allOf(
                                     CompletableFuture.runAsync(grpc, executor),
                                     CompletableFuture.runAsync(rest, executor),
                                     CompletableFuture.runAsync(restProtobuf, executor)
                             )
                             .whenComplete((r, t) -> {
                                 List<Metric> metrics = buildMetrics(rest, restProtobuf, grpc);
                                 writeResultToFile(metrics);

                                 System.out.println("=================================");
                                 System.out.printf("REST response time max = %d milli%n", rest.getMillis().stream().mapToLong(Duration::toMillis).max().getAsLong());
                                 System.out.printf("REST response time min = %d milli%n", rest.getMillis().stream().mapToLong(Duration::toMillis).min().getAsLong());
                                 System.out.printf("Prot response time max = %d milli%n", restProtobuf.getMillis().stream().mapToLong(Duration::toMillis).max().getAsLong());
                                 System.out.printf("Prot response time min = %d milli%n", restProtobuf.getMillis().stream().mapToLong(Duration::toMillis).min().getAsLong());
                                 System.out.printf("gRPC response time max = %d milli%n", grpc.getMillis().stream().mapToLong(Duration::toMillis).max().getAsLong());
                                 System.out.printf("gRPC response time min = %d milli%n", grpc.getMillis().stream().mapToLong(Duration::toMillis).min().getAsLong());
                                 System.out.println("=================================");
                                 double restAvg = printAndGetAvg(rest.getMillis(), "rest");
                                 double protAvg = printAndGetAvg(restProtobuf.getMillis(), "prot");
                                 double grpcAvg = printAndGetAvg(grpc.getMillis(), "grpc");
                                 System.out.println("=================================");
                                 System.out.printf("Prot gain = %.2f%s%n", (100 * (restAvg - protAvg) / restAvg), "%");
                                 System.out.printf("gRPC gain = %.2f%s%n", (100 * (restAvg - grpcAvg) / restAvg), "%");
                                 System.out.println("=================================");
                                 System.out.printf("gRPC and Protobuf request payload size = %d bytes%n", restProtobuf.requestPayloadSize());
                                 System.out.printf("REST request payload size = %d bytes%n", rest.requestPayloadSize());
                                 System.out.printf("Request payload gain = %.2f%s%n",
                                         (100d * (rest.requestPayloadSize() - restProtobuf.requestPayloadSize()) / rest.requestPayloadSize()),
                                         "%"
                                 );
                                 System.out.println("=================================");
                                 System.out.printf("gRPC and Protobuf response payload size = %d bytes%n", restProtobuf.responsePayloadSize());
                                 System.out.printf("REST response payload size = %d bytes%n", rest.responsePayloadSize());
                                 System.out.printf("Response payload gain = %.2f%s%n",
                                         (100d * (rest.responsePayloadSize() - restProtobuf.responsePayloadSize()) / rest.responsePayloadSize()),
                                         "%"
                                 );
                             })
                             .join();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            executor.shutdown();

            Duration duration = Duration.between(start, LocalDateTime.now());
            String durationTxT = "%ds.%n".formatted(duration.toSecondsPart());
            if (duration.toMinutesPart() > 0) {
                durationTxT = "%dm:%ds.%n".formatted(duration.toMinutes(), duration.toSecondsPart());
            }
            System.out.printf("%nTest completed in %s%n", durationTxT);
        }
        System.exit(0);
    }

    private static List<Metric> buildMetrics(Rest rest, RestProtobuf restProtobuf, Grpc grpc) {
        List<Metric> metics = new ArrayList<>(NUMBER_OF_CALLS);
        for (int i = 0; i < NUMBER_OF_CALLS; i++) {
            metics.add(new Metric(
                    rest.getMillis().get(i).toMillis(),
                    restProtobuf.getMillis().get(i).toMillis(),
                    grpc.getMillis().get(i).toMillis()

            ));
        }
        return metics;
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
                          .append(";").append(idx++ % 100 == 0 ? String.valueOf(idx) : "")
                          .append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static double printAndGetAvg(List<Duration> durations, String name) {
        double avg = (double) durations.stream().mapToLong(Duration::toMillis).sum() / NUMBER_OF_CALLS;
        System.out.printf("Response time AVG(%s) = %.2f milli%n", name, avg);
        return avg;
    }

}