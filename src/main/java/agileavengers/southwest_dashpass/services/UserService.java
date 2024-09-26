package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void saveUser(User user) {
        // Save user (could be Employee or Customer)
        userRepository.save(user);
    }

}

