package pawg.grpc.springgrpcserver;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatisticRepository extends MongoRepository<StatisticEntity, String> {
    Optional<StatisticEntity> findByUsername(String username);
}
