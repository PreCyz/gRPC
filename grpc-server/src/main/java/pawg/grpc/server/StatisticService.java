package pawg.grpc.server;

import io.grpc.stub.StreamObserver;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.statistics.StatisticGrpc;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;

public class StatisticService extends StatisticGrpc.StatisticImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);

    @Override
    public void getStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received fetch request from client: {}", request.getId());

        StatisticResponse response = StatisticResponse.newBuilder()
                .setId(request.getId())
                .setUsername("Zimorodek")
                .setApplicationVersion("1.0")
                .setFirstExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setLastExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setLastFailedDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setJavaVersion(System.getProperty("java.version"))
                .setLastRunType("MANUAL")
                .setLastSuccessDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setLastUpdateStatus("PARTIAL")
                .setStatus("FETCHED")
                .addSystemUsers("pawg")
                .addSystemUsers("precyz")
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", response);
    }

    @Override
    public void upsertStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received upsert request from client: {}", request.getId());

        StatisticResponse response = StatisticResponse.newBuilder()
                .setId(request.getId())
                .setUsername(request.getUsername())
                .setStatus("UPDATED")
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", response);
    }

    @Override
    public void deleteStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received delete request from client: {}", request.getId());

        StatisticResponse response = StatisticResponse.newBuilder()
                .setId(request.getId())
                .setStatus("DELETED")
                .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
    }
}
