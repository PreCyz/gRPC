package pawg.grpc.server;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pawg.grpc.service.statistics.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatisticService extends StatisticGrpc.StatisticImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);

    @Override
    public void postStatistics(RequestCollection request, StreamObserver<ResponseCollection> responseObserver) {
        logger.info("Received post request size : {}", request.getStatisticsCount());
        List<StatisticResponse> list = request.getStatisticsList().stream().map(this::createStatisticResponse).toList();

        ResponseCollection responseCollection = ResponseCollection.newBuilder().addAllStatistics(list).build();
        // Send the response
        responseObserver.onNext(responseCollection);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response with {} entries.", responseCollection.getSerializedSize());
    }

    private StatisticResponse createStatisticResponse(StatisticRequest request) {
        return StatisticResponse.newBuilder()
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
    }

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
}
