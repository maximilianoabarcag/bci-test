package cl.bci.test.service.impl;

import cl.bci.test.exception.EmailAlreadyExistsException;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.persistence.repository.UserRepository;
import cl.bci.test.service.TokenService;
import cl.bci.test.service.bo.PhoneInfoBo;
import cl.bci.test.service.bo.RegisterUserRequestBo;
import cl.bci.test.service.bo.RegisterUserResponseBo;
import cl.bci.test.service.bo.UserInfoRequestBo;
import cl.bci.test.service.bo.UserInfoResponseBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registraUsuarioNormalizandoDatosYHasheandoLaContrasena() {
        when(userRepository.existsByEmail("juan@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("Hunter22")).thenReturn("hash");
        when(tokenService.generateToken("juan@test.cl")).thenReturn("jwt");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserResponseBo response = userService.register(request("  Juan Rodriguez  ", "  Juan@Test.CL "));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).saveAndFlush(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Juan Rodriguez");
        assertThat(saved.getEmail()).isEqualTo("juan@test.cl");
        assertThat(saved.getPassword()).isEqualTo("hash");
        assertThat(saved.getToken()).isEqualTo("jwt");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getPhones()).hasSize(1);
        assertThat(saved.getPhones().getFirst().getUser()).isSameAs(saved);

        assertThat(response.getId()).isEqualTo(saved.getUuid());
        assertThat(response.getName()).isEqualTo("Juan Rodriguez");
        assertThat(response.getEmail()).isEqualTo("juan@test.cl");
        assertThat(response.getToken()).isEqualTo("jwt");
        assertThat(response.getPhones()).singleElement()
                .satisfies(phone -> {
                    assertThat(phone.getNumber()).isEqualTo("1234567");
                    assertThat(phone.getCityCode()).isEqualTo("1");
                    assertThat(phone.getCountryCode()).isEqualTo("57");
                });
    }

    @Test
    void registraUsuarioSinTelefonos() {
        when(userRepository.existsByEmail("juan@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("Hunter22")).thenReturn("hash");
        when(tokenService.generateToken("juan@test.cl")).thenReturn("jwt");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserRequestBo request = request("Juan", "juan@test.cl");
        request.setPhones(null);

        assertThat(userService.register(request).getPhones()).isEmpty();
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        when(userRepository.existsByEmail("juan@test.cl")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request("Juan", "JUAN@test.cl")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).saveAndFlush(any());
        verifyNoInteractions(passwordEncoder, tokenService);
    }

    @Test
    void consultaLaInformacionDelUsuario() {
        UserEntity user = new UserEntity();
        user.setName("Juan");
        user.setEmail("juan@test.cl");
        when(userRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(user));

        UserInfoResponseBo response = userService.getUserInfo(UserInfoRequestBo.builder().email("juan@test.cl").build());

        assertThat(response.getId()).isEqualTo(user.getUuid());
        assertThat(response.getEmail()).isEqualTo("juan@test.cl");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void fallaSiElUsuarioNoExiste() {
        when(userRepository.findByEmail("no@test.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(UserInfoRequestBo.builder().email("no@test.cl").build()))
                .isInstanceOf(NoSuchElementException.class);
    }

    private RegisterUserRequestBo request(String name, String email) {
        PhoneInfoBo phone = new PhoneInfoBo();
        phone.setNumber("1234567");
        phone.setCityCode("1");
        phone.setCountryCode("57");

        RegisterUserRequestBo request = new RegisterUserRequestBo();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("Hunter22");
        request.setPhones(List.of(phone));
        return request;
    }
}
