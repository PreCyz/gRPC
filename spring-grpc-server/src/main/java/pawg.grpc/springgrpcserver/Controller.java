package awesomepawg.springgrpc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class Controller {

    @GetMapping
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
}
