package pawg.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.statistics.StatisticGrpc;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;

public class StatisticClient {
    private static final Logger logger = LoggerFactory.getLogger(StatisticClient.class);

    private final ManagedChannel channel;
    private final StatisticGrpc.StatisticBlockingStub blockingStub;

    public StatisticClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Use plaintext for development, for production use TLS
                .build();
        blockingStub = StatisticGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void executePerson(String id) {
        logger.info("Will try to fetch {} ...", id);
        StatisticRequest request = StatisticRequest.newBuilder().setId(id).build();
        StatisticResponse response;
        try {
            response = blockingStub.getStatistic(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: {}", response.toString());
    }

    public static void main(String[] args) throws Exception {
        StatisticClient client = new StatisticClient("localhost", 50051);
//        StatisticClient client = new StatisticClient("localhost", 9090);
        try {
            client.executePerson("1");
            client.executePerson("2");
        } finally {
            client.shutdown();
        }
    }
}