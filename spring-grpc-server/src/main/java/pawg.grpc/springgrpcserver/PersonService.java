package pawg.grpc.springgrpcserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final StatisticRepository statisticRepository;

    @Autowired
    public PersonService(StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    public PersonEntity fetchPersonById(long id) {
        logger.info("Fetch person by id response to client: {}", id);
        Optional<Statistic> pawg = statisticRepository.findByUsername("PAWG");
        return pawg.map(s -> new PersonEntity(id, s.id, s.username))
                .orElseGet(() -> new PersonEntity(id, "default", "default"));
    }

    public PersonEntity upsertPerson(String name, String lastName) {
        logger.info("Upsert person: {} {}", name, lastName);

        return new PersonEntity(new Random().nextLong(100), name, lastName);
    }

    public void deletePersonById(long id) {
        logger.info("Delete person with id: {}", id);
    }
}
