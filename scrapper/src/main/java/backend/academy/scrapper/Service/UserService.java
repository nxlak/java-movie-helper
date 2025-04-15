package backend.academy.scrapper.Service;

import backend.academy.scrapper.Entity.User;
import backend.academy.scrapper.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(long chatId) {
        if (!isRegistered(chatId)) {
            User user = new User(chatId);
            userRepository.save(user);
        }
        else {
            throw new IllegalArgumentException("Already registered!");
        }
    }

    public boolean isRegistered(long chatId) {
        return userRepository.existsById(chatId);
    }

}
