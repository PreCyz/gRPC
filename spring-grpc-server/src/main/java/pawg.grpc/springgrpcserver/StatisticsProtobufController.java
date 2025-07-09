package pawg.grpc.springgrpcserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawg.grpc.service.statistics.StatisticRequest;
import pawg.grpc.service.statistics.StatisticResponse;
import pawg.grpc.service.statistics.StatisticResponse.Builder;

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

    @PutMapping(
            produces = "application/x-protobuf"
    )
    public ResponseEntity<StatisticResponse> update(@RequestBody StatisticRequest requestPayload) {
        var statisticEntity = new StatisticEntity();
        statisticEntity.id = requestPayload.getId();
        statisticEntity.username = requestPayload.getUsername();
        StatisticEntity statisticEntity1 = statisticService.upsertStatistic(statisticEntity);
        Builder responseBuilder = StatisticResponse.newBuilder()
                                                   .setId(requestPayload.getId())
                                                   .setUsername(requestPayload.getUsername())
                                                   .setFirstExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                   .setLastExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                   .setLastFailedDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                   .setJavaVersion(System.getProperty("java.version"))
                                                   .setLastRunType("MANUAL")
                                                   .setLastSuccessDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                                   .setLastUpdateStatus("PARTIAL")
                                                   .setApplicationVersion("1.0")
                                                   .addSystemUsers("systemUser")
                                                   .setStatus("UPDATED");
        return ResponseEntity.ok(responseBuilder.build());
    }
}
