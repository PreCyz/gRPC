package pawg.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GreetingServer {
    private static final Logger logger = LoggerFactory.getLogger(GreetingServer.class);

    private Server server;
    private static final int PORT = 50051;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new GreeterServiceImpl())
                .addService(new PersonServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                GreetingServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final GreetingServer server = new GreetingServer();
        server.start();
        server.blockUntilShutdown();
    }
}
