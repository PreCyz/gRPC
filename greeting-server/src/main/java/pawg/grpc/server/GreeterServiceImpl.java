package pawg.grpc.server;

import pawg.grpc.greeting.service.HelloRequest;
import pawg.grpc.greeting.service.HelloReply;
import pawg.grpc.greeting.service.GreeterGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GreeterServiceImpl.class);

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        logger.info("Received request from client: {}", request.getName());

        // Build the response
        String message = "Hello " + request.getName();
        HelloReply reply = HelloReply.newBuilder().setMessage(message).build();

        // Send the response
        responseObserver.onNext(reply);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", message);
    }
}
