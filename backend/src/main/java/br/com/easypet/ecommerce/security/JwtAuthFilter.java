package br.com.easypet.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        
        try {
            final String email = jwtService.extractEmail(token);
    
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Em um Resource Server puro, nós confiamos no token e nas roles informadas nele,
                // sem precisar ir ao banco de dados verificar se o usuário existe.
                if (jwtService.isTokenValid(token, email)) {
                    List<String> roles = jwtService.extractRoles(token);
                    
                    // Se não houver roles, podemos definir uma padrão, ou barrar
                    if (roles == null) {
                        roles = Collections.singletonList("ROLE_USER");
                    }
    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
    
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email, null, authorities
                            );
                            
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Em caso de token malformado ou expirado, não autentica
            logger.error("Token Inválido: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
