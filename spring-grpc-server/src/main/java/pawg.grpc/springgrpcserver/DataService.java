package pawg.grpc.springgrpcserver;

import java.util.List;

public interface DataService {
    StatisticEntity fetchStatisticByUsername(String username);

    List<StatisticEntity> fetchStatistics(List<StatisticEntity> statistics);
}
