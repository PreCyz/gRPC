package pawg.grpc;

import io.grpc.*;
import pawg.grpc.service.statistics.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GrpcClient {

    private final ManagedChannel channel;
    private final StatisticGrpc.StatisticBlockingStub blockingStub;

    public GrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                                       .usePlaintext() // Use plaintext for development, for production use TLS
                                       .build();
        blockingStub = StatisticGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public StatisticResponse fetchStatistic(String username) {
        try {
            StatisticRequest request = StatisticRequest.newBuilder().setUsername(username).build();
            return blockingStub.getStatistic(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace(System.err);
        }
        return StatisticResponse.newBuilder().build();
    }

    public ResponseCollection fetchStatistics(RequestCollection request) {
        try {
            return blockingStub.postStatistics(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace(System.err);
        }
        return ResponseCollection.newBuilder().build();
    }

    static RequestCollection buildRequestCollection(int numberOfRecords) {
        List<StatisticRequest> list = new ArrayList<>(numberOfRecords);
        for (int i = 0; i < numberOfRecords; i++) {
            list.add(StatisticRequest.newBuilder()
                    .setUsername("PAWG")
                    .setId(UUID.randomUUID().toString())
                    .build()
            );
        }
        return RequestCollection.newBuilder().addAllStatistics(list).build();
    }
}
