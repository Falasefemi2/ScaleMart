package com.femi.userservice.auth;

import com.femi.userservice.model.Role;
import com.femi.userservice.model.User;
import com.femi.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdminUser() {
        return args -> {
            String adminEmail = "femifalase228@gmail.com";

            if(userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .username("Femmie")
                        .role(Role.ADMIN)
                        .email(adminEmail)
                        .password(passwordEncoder.encode("Falasefemi228@"))
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Admin user created: " + adminEmail);
            }
        };
    }
}