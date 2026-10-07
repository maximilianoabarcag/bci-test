package cl.bci.test.service.impl;

import cl.bci.test.exception.InvalidCredentialsException;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.persistence.repository.UserRepository;
import cl.bci.test.service.TokenService;
import cl.bci.test.service.bo.LoginRequestBo;
import cl.bci.test.service.bo.LoginResponseBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void emiteUnTokenNuevoYActualizaElUltimoIngreso() {
        UserEntity user = user(true);
        LocalDateTime before = LocalDateTime.now();
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Hunter22", "hash")).thenReturn(true);
        when(tokenService.generateToken("juan@test.cl")).thenReturn("nuevo-jwt");
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        LoginResponseBo response = authService.login(request(" JUAN@test.cl ", "Hunter22"));

        assertThat(user.getToken()).isEqualTo("nuevo-jwt");
        assertThat(user.getLastLogin()).isAfterOrEqualTo(before);
        assertThat(response.getToken()).isEqualTo("nuevo-jwt");
        assertThat(response.getEmail()).isEqualTo("juan@test.cl");
    }

    @Test
    void rechazaContrasenaIncorrecta() {
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user(true)));
        when(passwordEncoder.matches("Otra1234", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request("juan@test.cl", "Otra1234")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void rechazaCorreoInexistente() {
        when(userRepository.findByEmail("no@test.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request("no@test.cl", "Hunter22")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rechazaUsuarioInactivo() {
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user(false)));

        assertThatThrownBy(() -> authService.login(request("juan@test.cl", "Hunter22")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private UserEntity user(boolean active) {
        UserEntity user = new UserEntity();
        user.setName("Juan");
        user.setEmail("juan@test.cl");
        user.setPassword("hash");
        user.setToken("jwt-anterior");
        user.setActive(active);
        return user;
    }

    private LoginRequestBo request(String email, String password) {
        LoginRequestBo request = new LoginRequestBo();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
