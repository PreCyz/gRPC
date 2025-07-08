package pawg.grpc.springgrpcserver;

import io.grpc.stub.StreamObserver;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import pawg.grpc.service.statistics.StatisticGrpc;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;
import pawg.grpc.service.statistics.StatisticResponse.Builder;

@GrpcService
public class StatisticsGRPC extends StatisticGrpc.StatisticImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsGRPC.class);

    private final StatisticService statisticService;

    @Autowired
    public StatisticsGRPC(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @Override
    public void getStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received fetch request from client: {}", request.getId());

        StatisticEntity statisticEntity = statisticService.fetchStatisticByUsername(request.getUsername());

        Builder responseBuilder = StatisticResponse.newBuilder()
                                                   .setId(request.getId())
                                                   .setUsername(request.getUsername())
                                                   .setApplicationVersion("1.0")
                                                   .setFirstExecutionDate(statisticEntity.firstExecutionDate)
                                                   .setLastExecutionDate(statisticEntity.lastExecutionDate)
                                                   .setLastFailedDate(statisticEntity.lastFailedDate)
                                                   .setJavaVersion(statisticEntity.javaVersion)
                                                   .setLastRunType(statisticEntity.lastRunType)
                                                   .setLastSuccessDate(statisticEntity.lastSuccessDate)
                                                   .setLastUpdateStatus(statisticEntity.lastUpdateStatus)
                                                   .setApplicationVersion(statisticEntity.applicationVersion)
                                                   .setStatus("FETCHED");

        statisticEntity.systemUsers.forEach(responseBuilder::addSystemUsers);

        // Send the response
        responseObserver.onNext(responseBuilder.build());

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", statisticEntity);
    }

    @Override
    public void upsertStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received upsert request from client: {}", request.getId());

        var entity = new StatisticEntity();
        entity.username = request.getUsername();

        StatisticEntity statisticEntity = statisticService.upsertStatistic(entity);

        StatisticResponse response = StatisticResponse.newBuilder()
                                                      .setId(request.getId())
                                                      .setUsername(request.getUsername())
                                                      .setApplicationVersion("1.0")
                                                      .setFirstExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                      .setLastExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                      .setLastFailedDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                      .setJavaVersion(System.getProperty("java.version"))
                                                      .setLastRunType("MANUAL")
                                                      .setLastSuccessDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                      .setLastUpdateStatus("PARTIAL")
                                                      .setStatus("UPDATED")
                                                      .build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response to client: {}", statisticEntity);
    }

    @Override
    public void deleteStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received delete request from client: {}", request.getId());

        statisticService.deleteStatisticById(request.getId());

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
