package pawg.grpc.type;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import pawg.grpc.service.statistics.RequestCollection;
import pawg.grpc.service.statistics.ResponseCollection;
import pawg.grpc.service.statistics.StatisticGrpc;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;

public class GrpcClient implements AutoCloseable{

    private final ManagedChannel channel;
    private final StatisticGrpc.StatisticBlockingStub blockingStub;

    public GrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                                       .usePlaintext() // Use plaintext for development, for production use TLS
                                       .build();
        blockingStub = StatisticGrpc.newBlockingStub(channel);
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

    public static RequestCollection buildRequestCollection(int numberOfRecords, String username) {
        List<StatisticRequest> list = new ArrayList<>(numberOfRecords);
        for (int i = 0; i < numberOfRecords; i++) {
            list.add(StatisticRequest.newBuilder()
                    .setUsername(username)
                    .setId(UUID.randomUUID().toString())
                    .build()
            );
        }
        return RequestCollection.newBuilder().addAllStatistics(list).build();
    }

    @Override
    public void close() throws Exception {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}
