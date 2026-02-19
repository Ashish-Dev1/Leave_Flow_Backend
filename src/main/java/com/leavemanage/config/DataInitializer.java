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

        if (userRepository.findByRole(Role.MANAGER).isEmpty()) {

            User manager = new User();
            manager.setName("Admin Manager");
            manager.setEmail("princedev2112@gmail.com");
            manager.setPassword(encoder.encode("admin123"));
            manager.setRole(Role.MANAGER);

            userRepository.save(manager);
        }
    }
}
