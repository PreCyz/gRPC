package pawg.grpc.springgrpcserver;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

@Profile("mongo")
public interface StatisticRepository extends MongoRepository<StatisticEntity, String> {
    Optional<StatisticEntity> findByUsername(String username);
}
