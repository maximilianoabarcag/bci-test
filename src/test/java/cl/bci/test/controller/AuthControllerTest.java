package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.controller.dto.LoginRequestDto;
import cl.bci.test.controller.dto.LoginResponseDto;
import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.exception.InvalidCredentialsException;
import cl.bci.test.service.AuthService;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.LoginRequestBo;
import cl.bci.test.service.bo.LoginResponseBo;
import cl.bci.test.service.bo.MessageBo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private MessageService messageService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
        ReflectionTestUtils.setField(controller, "messageService", messageService);
    }

    @Test
    void loginDelegaEnElServicioYRetornaElTokenNuevo() {
        LocalDateTime now = LocalDateTime.now();
        when(messageService.get(MessageCodeType.SUCCESS)).thenReturn(new MessageBo("0000", "Operación realizada con éxito"));
        when(authService.login(any(LoginRequestBo.class))).thenReturn(LoginResponseBo.builder()
                .id(UUID.randomUUID())
                .name("Juan Rodriguez")
                .email("juan@test.cl")
                .phones(List.of())
                .created(now.minusHours(1))
                .modified(now)
                .lastLogin(now)
                .token("nuevo-jwt")
                .active(true)
                .build());

        BaseResponseDto<LoginResponseDto> response = controller.login(request("juan@test.cl", "Hunter22"));

        ArgumentCaptor<LoginRequestBo> captor = ArgumentCaptor.forClass(LoginRequestBo.class);
        verify(authService).login(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("juan@test.cl");
        assertThat(captor.getValue().getPassword()).isEqualTo("Hunter22");
        assertThat(response.getCode()).isEqualTo("0000");
        assertThat(response.getData().getToken()).isEqualTo("nuevo-jwt");
        assertThat(response.getData().getLastLogin()).isEqualTo(now);
    }

    @Test
    void loginPropagaLasCredencialesInvalidas() {
        when(authService.login(any(LoginRequestBo.class))).thenThrow(new InvalidCredentialsException());

        assertThatThrownBy(() -> controller.login(request("juan@test.cl", "Otra1234")))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(messageService);
    }

    private LoginRequestDto request(String email, String password) {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
