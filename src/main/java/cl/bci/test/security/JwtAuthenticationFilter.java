package cl.bci.test.security;

import cl.bci.test.exception.InvalidTokenException;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.persistence.repository.UserRepository;
import cl.bci.test.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String SECURITY_ERROR_CODE = "securityErrorCode";

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(BEARER_PREFIX.length()).trim();

            try {
                String email = tokenService.validateAndGetSubject(token);

                userRepository.findByEmail(email)
                        .filter(UserEntity::isActive)
                        .filter(user -> token.equals(user.getToken()))
                        .ifPresent(user -> {
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    user.getEmail(), null, List.of());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });

            } catch (InvalidTokenException e) {
                log.debug("Token JWT inválido", e);
                SecurityContextHolder.clearContext();
                request.setAttribute(SECURITY_ERROR_CODE, e.getMessageCode());
            }
        }

        filterChain.doFilter(request, response);
    }
}
