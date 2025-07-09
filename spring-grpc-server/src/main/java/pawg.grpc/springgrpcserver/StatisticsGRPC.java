package pawg.grpc.springgrpcserver;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import pawg.grpc.service.statistics.*;
import pawg.grpc.service.statistics.StatisticResponse.Builder;

import java.util.List;

@GrpcService
public class StatisticsGRPC extends StatisticGrpc.StatisticImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsGRPC.class);

    private final StatisticService statisticService;

    @Autowired
    public StatisticsGRPC(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @Override
    public void postStatistics(RequestCollection request, StreamObserver<ResponseCollection> responseObserver) {
        logger.info("Received post request size : {}", request.getStatisticsCount());

        StatisticEntity statisticEntity = statisticService.fetchStatisticByUsername(
                request.getStatisticsList().get(0).getUsername()
        );
        List<StatisticResponse> list = request.getStatisticsList()
                .stream()
                .map(req -> createStatisticResponse(statisticEntity))
                .toList();
        ResponseCollection responseCollection = ResponseCollection.newBuilder().addAllStatistics(list).build();

        // Send the response
        responseObserver.onNext(responseCollection);

        // Complete the RPC call
        responseObserver.onCompleted();
        logger.info("Sent response with [{}] entries.", responseCollection.getSerializedSize());
    }

    public static StatisticResponse createStatisticResponse(StatisticEntity entity) {
        return StatisticResponse.newBuilder()
                .setId(entity.id)
                .setUsername(entity.username)
                .setApplicationVersion(entity.applicationVersion)
                .setFirstExecutionDate(entity.firstExecutionDate)
                .setLastExecutionDate(entity.lastExecutionDate)
                .setLastFailedDate(entity.lastFailedDate)
                .setJavaVersion(entity.javaVersion)
                .setLastRunType(entity.lastRunType)
                .setLastSuccessDate(entity.lastSuccessDate)
                .setLastUpdateStatus(entity.lastUpdateStatus)
                .setStatus(entity.lastUpdateStatus)
                .addSystemUsers("pawg")
                .build();
    }

    @Override
    public void getStatistic(StatisticRequest request, StreamObserver<StatisticResponse> responseObserver) {
        logger.info("Received fetch request from client: {}", request.getUsername());

        StatisticEntity statisticEntity = statisticService.fetchStatisticByUsername(request.getUsername());

        Builder responseBuilder = StatisticResponse.newBuilder()
                                                   .setId(statisticEntity.id)
                                                   .setUsername(request.getUsername())
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
}
