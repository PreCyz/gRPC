package pawg.grpc.springgrpcserver;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;
import pawg.grpc.service.greeting.GreeterGrpc;
import pawg.grpc.service.greeting.HelloReply;
import pawg.grpc.service.greeting.HelloRequest;

@GrpcService
public class GreeterServerImpl extends GreeterGrpc.GreeterImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GreeterServerImpl.class);

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
