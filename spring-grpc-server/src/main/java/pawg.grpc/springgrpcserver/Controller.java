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
@RequestMapping(path = "/person")
public class Controller {

    private final PersonService personService;

    @Autowired
    public Controller(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{personId}")
    public ResponseEntity<PersonEntity> getPersonById(@PathVariable Long personId) {
        return ResponseEntity.ok(personService.fetchPersonById(personId));
    }

    @PutMapping
    public ResponseEntity<PersonEntity> update(PersonEntity personEntity) {
        return ResponseEntity.ok(personService.upsertPerson(personEntity.name(), personEntity.lastName()));
    }

    @DeleteMapping("/{personId}")
    public ResponseEntity<Void> deleteById(@PathVariable Long personId) {
        personService.fetchPersonById(personId);
        return ResponseEntity.ok(null);
    }
}
