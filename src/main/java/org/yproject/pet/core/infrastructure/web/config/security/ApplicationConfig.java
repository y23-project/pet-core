package org.yproject.pet.core.infrastructure.web.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.yproject.pet.core.domain.User;
import org.yproject.pet.core.application.user.UserStorage;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserStorage userStorage;

    @Bean
    public AuthenticationProvider authenticationProvider(
            final UserDetailsService userDetailsService,
            final PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return email -> {
            Optional<User> userOptional = userStorage.findByEmail(email);
            if (userOptional.isEmpty()) {
                throw new UsernameNotFoundException(email);
            }
            return new UserInfo(userOptional.get());
        };
    }
}