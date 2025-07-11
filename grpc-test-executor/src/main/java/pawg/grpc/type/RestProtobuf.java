package pawg.grpc.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import pawg.grpc.service.statistics.RequestCollection;
import pawg.grpc.service.statistics.ResponseCollection;

public class RestProtobuf extends Rest {
    private final RequestCollection requestCollection;

    public RestProtobuf(HttpClient httpClient, int numberOfCalls, int numberOfRecords, String username, URI restUri) {
        super(httpClient, numberOfCalls, numberOfRecords, username, restUri);
        this.requestCollection = GrpcClient.buildRequestCollection(numberOfRecords, username);
    }

    @Override
    protected void execute(int requestNumber) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            requestCollection.writeTo(out);
            try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {

                HttpRequest post = HttpRequest.newBuilder()
                                              .POST(HttpRequest.BodyPublishers.ofInputStream(() -> in))
                                              .uri(restUri)
                                              .header("Content-Type", "application/x-protobuf")
                                              .header("Accept", "application/x-protobuf")
                                              .build();

                try (InputStream is = httpClient.send(post, BodyHandlers.ofInputStream()).body()) {
                    ResponseCollection response = ResponseCollection.parseFrom(is);
                    if (requestNumber % 100 == 0) {
                        responsePayloadSize = response.toByteArray().length;
                        System.out.printf("%d. Protobuf call completed. Number of records in payload [%d]. Payload size [%d]. Response size [%d]%n",
                                requestNumber, response.getStatisticsCount(), out.size(), response.toByteArray().length);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public int requestPayloadSize() {
        return requestCollection.toByteArray().length;
    }

    @Override
    public int responsePayloadSize() {
        return super.responsePayloadSize();
    }
}
