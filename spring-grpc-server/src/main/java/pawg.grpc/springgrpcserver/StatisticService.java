package pawg.grpc.springgrpcserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!mongo")
public class StatisticService implements DataService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);

    @Override
    public StatisticEntity fetchStatisticByUsername(String username) {
        logger.info("Fetch stat by username: {}", username);
        return createStatisticEntity(username);
    }

    private StatisticEntity createStatisticEntity(String username) {
        StatisticEntity statisticEntity = new StatisticEntity();
        statisticEntity.id = UUID.randomUUID().toString();
        statisticEntity.username = username;
        statisticEntity.lastExecutionDate =  LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        statisticEntity.firstExecutionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        statisticEntity.lastSuccessDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        statisticEntity.lastFailedDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        statisticEntity.javaVersion = System.getProperty("java.version");
        statisticEntity.lastUpdateStatus = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        statisticEntity.lastRunType = "MANUAL";
        statisticEntity.systemUsers = Set.of(System.getProperty("user.name"));
        statisticEntity.applicationVersion = "1.0";
        statisticEntity.status = "CREATED";
        return statisticEntity;
    }

    @Override
    public List<StatisticEntity> fetchStatistics(List<StatisticEntity> statistics) {
        logger.info("Fetch stat count: {}", statistics.size());
        StatisticEntity statisticEntity = createStatisticEntity(statistics.get(0).username);
        return statistics.stream().map(s -> statisticEntity).toList();
    }
}
