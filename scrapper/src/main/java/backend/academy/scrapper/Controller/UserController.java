package backend.academy.scrapper.Controller;

import backend.academy.scrapper.Repository.UserRepository;
import backend.academy.scrapper.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<?> registerChat(@PathVariable long chatId) {

        try {
            userService.register(chatId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();

    }

}
