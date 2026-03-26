package org.bbqqvv.backendecommerce.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.AuthProvider;
import org.bbqqvv.backendecommerce.entity.Role;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        String adminUsername = "vanbui262004";
        String defaultPassword = "123123";

        userRepository.findByUsername(adminUsername).ifPresentOrElse(
            user -> {
                log.info("Admin user '{}' already exists. Synchronizing password to default '123123' for debugging...", adminUsername);
                user.setPassword(passwordEncoder.encode(defaultPassword));
                user.setAuthorities(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
                user.setEnabled(true);
                userRepository.save(user);
                log.info("Admin user '{}' password synchronized successfully.", adminUsername);
            },
            () -> {
                log.info("Admin user '{}' not found. Creating a new one...", adminUsername);
                User admin = User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(defaultPassword))
                        .email("vanbui0966467356@gmail.com")
                        .name("Van Bui Quoc (Admin)")
                        .enabled(true)
                        .provider(AuthProvider.LOCAL)
                        .authorities(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                        .build();
                userRepository.save(admin);
                log.info("Admin user '{}' created successfully with password '123123'.", adminUsername);
            }
        );
    }
}
