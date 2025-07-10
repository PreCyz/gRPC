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

@RestController
@RequestMapping(path = "/statistics")
public class StatisticsController {

    private final DataService dataService;

    @Autowired
    public StatisticsController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<StatisticEntity> getPersonById(@PathVariable String username) {
        return ResponseEntity.ok(dataService.fetchStatisticByUsername(username));
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<List<StatisticEntity>> postStatistics(@RequestBody List<StatisticEntity> statistics) {
        return ResponseEntity.ok(dataService.fetchStatistics(statistics));
    }


}
