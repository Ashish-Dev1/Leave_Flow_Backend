package com.leavemanage.config;

import com.leavemanage.model.Role;
import com.leavemanage.model.User;
import com.leavemanage.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        if (userRepository.findAllByRole(Role.MANAGER).isEmpty()) {

            User manager1 = new User();
            manager1.setName("Admin ");
            manager1.setEmail("admin@gmail.com");
            manager1.setPassword(encoder.encode("Admin@123"));
            manager1.setRole(Role.MANAGER);
            User manager2 = new User();
            manager2.setName("HR Manager");
            manager2.setEmail("hr@gmail.com");
            manager2.setPassword(encoder.encode("Hr@12345"));
            manager2.setRole(Role.MANAGER);
            userRepository.save(manager1);
            userRepository.save(manager2);
        }
    }
}
