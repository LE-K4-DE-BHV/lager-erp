package com.lagermanagement.space.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var operator = User.builder()
                .username("operator")
                .password(securityProperties.getOperatorPasswordHash())
                .roles("OPERATOR")
                .build();
        return new InMemoryUserDetailsManager(operator);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic
                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/v1/auth/login")
                .successHandler(jsonLoginSuccessHandler())
                .failureHandler(jsonLoginFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
            );

        return http.build();
    }

    private AuthenticationSuccessHandler jsonLoginSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                Map.of("username", authentication.getName()));
        };
    }

    private AuthenticationFailureHandler jsonLoginFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                Map.of("status", 401, "message", "Ungültige Anmeldedaten"));
        };
    }

    private org.springframework.security.web.AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                Map.of("status", 401, "message", "Nicht authentifiziert"));
        };
    }
}
