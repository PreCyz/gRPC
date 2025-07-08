package pawg.grpc.springgrpcserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping
    public ResponseEntity<StatisticEntity> update(StatisticEntity statisticEntity) {
        return ResponseEntity.ok(statisticService.upsertStatistic(statisticEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        statisticService.deleteStatisticById(id);
        return ResponseEntity.ok(null);
    }
}
