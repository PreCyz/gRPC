package pawg.grpc.springgrpcserver;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawg.grpc.service.statistics.RequestCollection;
import pawg.grpc.service.statistics.ResponseCollection;
import pawg.grpc.service.statistics.StatisticResponse;
import pawg.grpc.service.statistics.StatisticResponse.Builder;

@RestController
@RequestMapping(path = "/statistics/protobuf")
public class StatisticsProtobufController {

    private final DataService dataService;

    @Autowired
    public StatisticsProtobufController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping(
            path = "/{username}",
            produces = "application/x-protobuf"
    )
    public ResponseEntity<StatisticResponse> getPersonById(@PathVariable String username) {
        StatisticEntity statisticEntity = dataService.fetchStatisticByUsername(username);
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
        StatisticEntity statisticEntity = dataService.fetchStatisticByUsername(
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
