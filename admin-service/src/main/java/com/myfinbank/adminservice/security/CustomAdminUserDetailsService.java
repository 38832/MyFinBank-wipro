package com.myfinbank.adminservice.security;

import com.myfinbank.adminservice.entity.AdminUser;
import com.myfinbank.adminservice.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomAdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository repository;

    public CustomAdminUserDetailsService(AdminUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser admin = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
        return new AdminUserDetails(admin);
    }
}
