package pawg.grpc.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.statistics.*;

import java.util.concurrent.TimeUnit;

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

    public void executePerson(String username) {
        logger.info("Will try to fetch {} ...", username);
        StatisticRequest request = StatisticRequest.newBuilder().setUsername(username).build();
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
//        StatisticClient client = new StatisticClient("localhost", 50051);
        StatisticClient client = new StatisticClient("localhost", 9090);
        try {
            client.executePerson("PAWG");
            client.executePerson("PAWG");
        } finally {
            client.shutdown();
        }
    }
}