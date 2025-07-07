package pawg.grpc.server;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.person.*;

import java.util.UUID;

public class PersonServiceImpl extends PersonGrpc.PersonImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    @Override
    public void getPerson(PersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        logger.info("Received fetch request from client: {}", request.getId());

        PersonResponse response = PersonResponse.newBuilder()
                .setId(request.getId())
                .setName(UUID.randomUUID().toString())
                .setLastName("Zimorodek")
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", response);
    }

    @Override
    public void upsertPerson(PersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        logger.info("Received upsert request from client: {}", request.getId());

        PersonResponse response = PersonResponse.newBuilder()
                .setId(request.getId())
                .setName(request.getName())
                .setLastName(request.getLastName())
                .setStatus("UPDATED")
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", response);
    }

    @Override
    public void deletePerson(PersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        logger.info("Received delete request from client: {}", request.getId());

        PersonResponse response = PersonResponse.newBuilder()
                .setId(request.getId())
                .setStatus("DELETED")
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
    }
}
