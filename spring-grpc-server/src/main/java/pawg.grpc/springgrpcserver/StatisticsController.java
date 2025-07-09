package pawg.grpc.springgrpcserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/statistics")
public class StatisticsController {

    private final StatisticService statisticService;

    @Autowired
    public StatisticsController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<StatisticEntity> getPersonById(@PathVariable String username) {
        return ResponseEntity.ok(statisticService.fetchStatisticByUsername(username));
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<List<StatisticEntity>> postStatistics(@RequestBody List<StatisticEntity> statistics) {
        return ResponseEntity.ok(statisticService.fetchStatistics(statistics));
    }


}
