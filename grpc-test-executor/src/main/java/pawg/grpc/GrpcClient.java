package pawg.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.statistics.StatisticGrpc;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;

public class GrpcClient {
    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

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
        logger.info("Will try to fetch {} ...", username);
        StatisticRequest request = StatisticRequest.newBuilder().setUsername(username).build();

        try {
            StatisticResponse response = blockingStub.getStatistic(request);
            logger.info("Greeting: {}", response.toString());
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
        }
        return StatisticResponse.newBuilder().build();
    }
}
