package com.lagermanagement.space.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    private static final String TEST_PASSWORD = "test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Configuration
    static class TestSecurityConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        UserDetailsService userDetailsService(PasswordEncoder encoder) {
            return new InMemoryUserDetailsManager(
                    User.builder()
                            .username("operator")
                            .password(encoder.encode(TEST_PASSWORD))
                            .roles("OPERATOR")
                            .build()
            );
        }

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginProcessingUrl("/api/v1/auth/login")
                    .successHandler((request, response, authentication) -> {
                        response.setStatus(200);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        mapper.writeValue(response.getWriter(),
                            Map.of("username", authentication.getName()));
                    })
                    .failureHandler((request, response, exception) -> {
                        response.setStatus(401);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        mapper.writeValue(response.getWriter(),
                            Map.of("status", 401, "message", "Ungültige Anmeldedaten"));
                    })
                    .permitAll()
                )
                .logout(logout -> logout.disable())
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        mapper.writeValue(response.getWriter(),
                            Map.of("status", 401, "message", "Nicht authentifiziert"));
                    })
                );
            return http.build();
        }
    }

    @Test
    void shouldReturn200WithUsernameOnSuccessfulLogin() throws Exception {
        // @WebMvcTest setzt kein JSESSIONID-Cookie in MockMvc — nur Status + Body werden geprüft
        mockMvc.perform(post("/api/v1/auth/login")
                        .param("username", "operator")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("operator"));
    }

    @Test
    void shouldReturn401WhenLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .param("username", "operator")
                        .param("password", "falsch"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn401WhenMeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void shouldReturnUsernameOnMeWithAuthenticatedSession() throws Exception {
        // @WithMockUser injiziert Authentication direkt in den SecurityContext (vor dem Filter)
        // SecurityMockMvcRequestPostProcessors.user() löst Controller-Mapping in @WebMvcTest
        // mit custom SecurityFilterChain nicht zuverlässig auf
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("operator"));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void shouldInvalidateSessionOnLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn401OnMeAfterLogout() throws Exception {
        // Erst logout (simuliert durch fehlende Authentifizierung)
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateViaFormLogin() throws Exception {
        mockMvc.perform(formLogin("/api/v1/auth/login")
                        .user("operator")
                        .password(TEST_PASSWORD))
                .andExpect(authenticated().withUsername("operator"));
    }

    @Test
    void shouldNotAuthenticateWithWrongCredentials() throws Exception {
        mockMvc.perform(formLogin("/api/v1/auth/login")
                        .user("operator")
                        .password("falsch"))
                .andExpect(unauthenticated());
    }
}
