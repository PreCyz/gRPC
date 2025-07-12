package pawg.grpc.type;

import pawg.grpc.service.statistics.RequestCollection;
import pawg.grpc.service.statistics.ResponseCollection;

public class Grpc extends AbstractBase {
    private final GrpcClient grpcClient;
    private final RequestCollection requestCollection;

    public Grpc(GrpcClient grpcClient, int numberOfCalls, int numberOfRecords, String username) {
        super(numberOfCalls, numberOfRecords, username);
        this.grpcClient = grpcClient;
        this.requestCollection = GrpcClient.buildRequestCollection(numberOfRecords, username);
    }

    @Override
    protected void execute(int requestNumber) {
        ResponseCollection response = grpcClient.fetchStatistics(requestCollection);
        if (requestNumber % 100 == 0) {
            responsePayloadSize = response.toByteArray().length;
            System.out.printf("%d. gRPC call completed. Number of records in payload [%d]. Request payload size [%d]. Response size [%d].%n",
                    requestNumber, response.getStatisticsCount(), requestCollection.toByteArray().length, responsePayloadSize);
        }
    }

    @Override
    public int requestPayloadSize() {
        return requestCollection.toByteArray().length;
    }

    @Override
    public int responsePayloadSize() {
        return responsePayloadSize;
    }
}
