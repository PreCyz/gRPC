package pawg.grpc.springgrpcserver;

import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    public PersonEntity fetchPersonById(long id) {
        logger.info("Fetch person by id response to client: {}", id);
        return new PersonEntity(id, UUID.randomUUID().toString(), "Zimorodek");
    }

    public PersonEntity upsertPerson(String name, String lastName) {
        logger.info("Upsert person: {} {}", name, lastName);

        return new PersonEntity(new Random().nextLong(100), name, lastName);
    }

    public void deletePersonById(long id) {
        logger.info("Delete person with id: {}", id);
    }
}
