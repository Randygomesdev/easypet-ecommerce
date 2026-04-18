package br.com.easypet.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secure")
public class TestController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        // Pega as roles geradas a partir do Token do Easypet Original!
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        response.put("email", authentication.getName());
        response.put("roles_interceptadas", roles);
        response.put("mensagem", "Parabéns! O Resource Server (Ecommerce) validou o token do Easypet perfeitamente!");

        return ResponseEntity.ok(response);
    }
}
