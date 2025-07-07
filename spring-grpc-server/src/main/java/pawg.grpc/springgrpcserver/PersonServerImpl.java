package pawg.grpc.springgrpcserver;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import pawg.grpc.service.person.PersonGrpc;
import pawg.grpc.service.person.PersonRequest;
import pawg.grpc.service.person.PersonResponse;

@GrpcService
public class PersonServerImpl extends PersonGrpc.PersonImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PersonServerImpl.class);

    private PersonService personService;

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void getPerson(PersonRequest request, StreamObserver<PersonResponse> responseObserver) {
        logger.info("Received fetch request from client: {}", request.getId());

        PersonEntity personEntity = personService.fetchPersonById(request.getId());

        PersonResponse response = PersonResponse.newBuilder()
                                                .setId(personEntity.id())
                                                .setName(personEntity.name())
                                                .setLastName(personEntity.lastName())
                                                .setStatus("FETCHED")
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

        PersonEntity personEntity = personService.upsertPerson(request.getName(), request.getLastName());

        PersonResponse response = PersonResponse.newBuilder()
                                                .setId(personEntity.id())
                                                .setName(personEntity.name())
                                                .setLastName(personEntity.lastName())
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

        personService.deletePersonById(request.getId());

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
