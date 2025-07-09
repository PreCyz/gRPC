package pawg.grpc.springgrpcserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawg.grpc.service.statistics.*;
import pawg.grpc.service.statistics.StatisticResponse.Builder;

import java.util.List;

@RestController
@RequestMapping(path = "/statistics/protobuf")
public class StatisticsProtobufController {

    private final StatisticService statisticService;

    @Autowired
    public StatisticsProtobufController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping(
            path = "/{username}",
            produces = "application/x-protobuf"
    )
    public ResponseEntity<StatisticResponse> getPersonById(@PathVariable String username) {
        StatisticEntity statisticEntity = statisticService.fetchStatisticByUsername(username);
        Builder responseBuilder = StatisticResponse.newBuilder()
                                                   .setId(statisticEntity.id)
                                                   .setUsername(username)
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
        return ResponseEntity.ok(responseBuilder.build());
    }

    @PostMapping(
            consumes = "application/x-protobuf",
            produces = "application/x-protobuf"
    )
    public ResponseEntity<ResponseCollection> getPersonById(@RequestBody RequestCollection request) {
        StatisticEntity statisticEntity = statisticService.fetchStatisticByUsername(
                request.getStatisticsList().get(0).getUsername()
        );

        List<StatisticResponse> list = request.getStatisticsList()
                .stream()
                .map(r -> StatisticsGRPC.createStatisticResponse(statisticEntity))
                .toList();

        ResponseCollection responseCollection = ResponseCollection.newBuilder().addAllStatistics(list).build();

        return ResponseEntity.ok(responseCollection);
    }

}
