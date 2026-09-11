package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.web.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Auth-Endpunkte. Der Login-Endpunkt (/api/v1/auth/login) wird vollständig von
 * Spring Securitys formLogin-Filter abgefangen und erreicht diesen Controller nicht.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return new AuthResponse(authentication.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
