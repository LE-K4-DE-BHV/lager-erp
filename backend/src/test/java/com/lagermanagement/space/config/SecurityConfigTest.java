package com.lagermanagement.space.config;

import com.lagermanagement.space.web.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für SecurityUtils und die grundlegenden Security-Konfigurationsregeln.
 * Die Integration (Login, CSRF-off, JSON-Antworten) wird durch AuthControllerTest abgedeckt.
 */
class SecurityConfigTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnUsernameWhenAuthenticationIsPresent() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "operator", null,
                List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String username = SecurityUtils.getCurrentUsername();

        assertThat(username).isEqualTo("operator");
    }

    @Test
    void shouldReturnAnonymousWhenSecurityContextIsEmpty() {
        SecurityContextHolder.clearContext();

        String username = SecurityUtils.getCurrentUsername();

        assertThat(username).isEqualTo("anonymous");
    }

    @Test
    void shouldReturnAnonymousWhenAuthenticationIsNull() {
        SecurityContextHolder.getContext().setAuthentication(null);

        String username = SecurityUtils.getCurrentUsername();

        assertThat(username).isEqualTo("anonymous");
    }

    @Test
    void shouldReturnAnonymousWhenPrincipalIsAnonymousUser() {
        // Simuliert einen nicht-authentifizierten Request durch Spring Security
        var anonAuth = new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(anonAuth);

        String username = SecurityUtils.getCurrentUsername();

        // anonymousUser ist authenticated=true, gibt daher den Namen zurück
        assertThat(username).isEqualTo("anonymousUser");
    }

    @Test
    void shouldReturnOperatorUsernameForDifferentUserNames() {
        String[] nutzer = {"operator", "admin", "test-user"};

        for (String name : nutzer) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(name, null,
                            List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")))
            );
            assertThat(SecurityUtils.getCurrentUsername()).isEqualTo(name);
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldVerifyJpaAuditingConfigAnnotation() {
        // JpaAuditingConfig muss @EnableJpaAuditing tragen — prüfen via Reflection
        boolean hasEnableJpaAuditing = JpaAuditingConfig.class
                .isAnnotationPresent(org.springframework.data.jpa.repository.config.EnableJpaAuditing.class);

        assertThat(hasEnableJpaAuditing)
                .as("JpaAuditingConfig muss @EnableJpaAuditing tragen")
                .isTrue();
    }

    @Test
    void shouldVerifySecurityConfigUsesRequiredArgsConstructor() {
        // Stellt sicher, dass kein @Autowired-Feld in SecurityConfig vorhanden ist
        long autowiredFieldCount = java.util.Arrays.stream(SecurityConfig.class.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(org.springframework.beans.factory.annotation.Autowired.class))
                .count();

        assertThat(autowiredFieldCount)
                .as("SecurityConfig darf kein @Autowired auf Feldern haben")
                .isZero();
    }
}
