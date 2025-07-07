package pawg.grpc.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.greeting.*;

import java.util.concurrent.TimeUnit;

public class GreetingClient {
    private static final Logger logger = LoggerFactory.getLogger(GreetingClient.class);

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public GreetingClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Use plaintext for development, for production use TLS
                .build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        logger.info("Will try to greet {} ...", name);
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: {}", response.getMessage());
    }

    public static void main(String[] args) throws Exception {
//        GreetingClient client = new GreetingClient("localhost", 50051);
        GreetingClient client = new GreetingClient("localhost", 9090);
        try {
            String user = "world";
            if (args.length > 0) {
                user = args[0]; // Use the arg as the user name if provided
            }
            client.greet(user);
        } finally {
            client.shutdown();
        }
    }
}