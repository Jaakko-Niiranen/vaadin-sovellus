package com.example.application.security;

import com.example.application.data.Role;
import com.example.application.data.User;
import com.example.application.data.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class InitialUsers implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createOrUpdateUser(
                "admin",
                "Admin",
                "admin123",
                Set.of(Role.ADMIN, Role.SUPER, Role.USER)
        );

        createOrUpdateUser(
                "user",
                "User",
                "user123",
                Set.of(Role.USER)
        );
    }

    private void createOrUpdateUser(String username, String name, String rawPassword, Set<Role> roles) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);

        user.setUsername(username);
        user.setName(name);
        user.setHashedPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(roles);

        userRepository.save(user);
    }
}
