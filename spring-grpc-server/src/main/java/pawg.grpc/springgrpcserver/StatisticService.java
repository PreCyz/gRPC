package pawg.grpc.springgrpcserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);

    private final StatisticRepository statisticRepository;

    @Autowired
    public StatisticService(StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    public StatisticEntity fetchStatisticByUsername(String username) {
        logger.info("Fetch stat by username: {}", username);
        StatisticEntity statisticEntity = statisticRepository.findByUsername(username).orElse(new StatisticEntity());
        statisticEntity.status = "FETCHED";
        return statisticEntity;
    }

    public List<StatisticEntity> fetchStatistics(List<StatisticEntity>  statistics) {
        logger.info("Fetch stat count: {}", statistics.size());
        StatisticEntity statisticEntity = statisticRepository.findByUsername(statistics.get(0).username)
                .orElse(new StatisticEntity());
        return statistics.stream().map(s -> statisticEntity).toList();
    }
}
