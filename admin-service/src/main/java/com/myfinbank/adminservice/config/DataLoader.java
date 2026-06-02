package com.myfinbank.adminservice.config;

import com.myfinbank.adminservice.entity.AdminUser;
import com.myfinbank.adminservice.repository.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadDefaultAdmin(AdminUserRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new AdminUser("admin@myfinbank.com", "Administrator", encoder.encode("admin123")));
            }
        };
    }
}
