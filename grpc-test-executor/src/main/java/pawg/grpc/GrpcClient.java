package pawg.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import pawg.grpc.service.statistics.StatisticGrpc;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;

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
}
