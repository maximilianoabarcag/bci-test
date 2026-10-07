package cl.bci.test.security;

import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.exception.InvalidTokenException;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.persistence.repository.UserRepository;
import cl.bci.test.service.TokenService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenService, userRepository);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autenticaCuandoElTokenEsValidoYEsElUltimoEmitido() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-vigente");
        when(tokenService.validateAndGetSubject("jwt-vigente")).thenReturn("juan@test.cl");
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user(true, "jwt-vigente")));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("juan@test.cl");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noAutenticaSinHeaderAuthorization() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenService, userRepository);
    }

    @Test
    void ignoraOtrosEsquemasDeAutenticacion() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenService, userRepository);
    }

    @Test
    void noAutenticaSiElTokenNoEsElUltimoEmitido() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-anterior");
        when(tokenService.validateAndGetSubject("jwt-anterior")).thenReturn("juan@test.cl");
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user(true, "jwt-nuevo")));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noAutenticaSiElUsuarioEstaInactivo() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-vigente");
        when(tokenService.validateAndGetSubject("jwt-vigente")).thenReturn("juan@test.cl");
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user(false, "jwt-vigente")));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noAutenticaSiElUsuarioNoExiste() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-vigente");
        when(tokenService.validateAndGetSubject("jwt-vigente")).thenReturn("no@test.cl");
        when(userRepository.findByEmail("no@test.cl")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void marcaElErrorCuandoElTokenEsInvalido() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-invalido");
        when(tokenService.validateAndGetSubject("jwt-invalido"))
                .thenThrow(new InvalidTokenException(new IllegalArgumentException("firma inválida")));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute(JwtAuthenticationFilter.SECURITY_ERROR_CODE)).isEqualTo(MessageCodeType.INVALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userRepository);
    }

    private UserEntity user(boolean active, String token) {
        UserEntity user = new UserEntity();
        user.setEmail("juan@test.cl");
        user.setToken(token);
        user.setActive(active);
        return user;
    }
}
