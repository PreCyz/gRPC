package pawg.grpc.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.person.*;

import java.util.concurrent.TimeUnit;

public class PersonClient {
    private static final Logger logger = LoggerFactory.getLogger(PersonClient.class);

    private final ManagedChannel channel;
    private final PersonGrpc.PersonBlockingStub blockingStub;

    public PersonClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Use plaintext for development, for production use TLS
                .build();
        blockingStub = PersonGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void executePerson(int id) {
        logger.info("Will try to fetch {} ...", id);
        PersonRequest request = PersonRequest.newBuilder().setId(id).build();
        PersonResponse response;
        try {
            response = blockingStub.getPerson(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: {}", response.toString());
    }

    public static void main(String[] args) throws Exception {
//        PersonClient client = new PersonClient("localhost", 50051);
        PersonClient client = new PersonClient("localhost", 9090);
        try {
            String user = "world";
            if (args.length > 0) {
                user = args[0]; // Use the arg as the user name if provided
            }
            client.executePerson(1);
        } finally {
            client.shutdown();
        }
    }
}