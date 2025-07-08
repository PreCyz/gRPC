package pawg.grpc.springgrpcserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return statisticRepository.findByUsername(username).orElse(new StatisticEntity());
    }

    public StatisticEntity upsertStatistic(StatisticEntity statisticEntity) {
        logger.info("Upsert person: [{}] ", statisticEntity);

        var statistic = new StatisticEntity();
        statistic.id = statisticEntity.id;
        statistic.username = statisticEntity.username;
        statistic.javaVersion = System.getProperty("java.version");
        return statistic;
    }

    public void deleteStatisticById(long id) {
        logger.info("Delete person with id: {}", id);
    }
}
