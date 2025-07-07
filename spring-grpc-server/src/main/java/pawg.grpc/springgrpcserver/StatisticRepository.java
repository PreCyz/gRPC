package pawg.grpc.springgrpcserver;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StatisticRepository extends MongoRepository<Statistic, String> {
    Optional<Statistic> findByUsername(String username);
}
