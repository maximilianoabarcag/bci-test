package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.controller.dto.PhoneInfoDto;
import cl.bci.test.controller.dto.RegisterUserRequestDto;
import cl.bci.test.controller.dto.RegisterUserResponseDto;
import cl.bci.test.controller.dto.UserInfoResponseDto;
import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.enums.ResponseStatusType;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.UserService;
import cl.bci.test.service.bo.MessageBo;
import cl.bci.test.service.bo.PhoneInfoBo;
import cl.bci.test.service.bo.RegisterUserRequestBo;
import cl.bci.test.service.bo.RegisterUserResponseBo;
import cl.bci.test.service.bo.UserInfoRequestBo;
import cl.bci.test.service.bo.UserInfoResponseBo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 7, 10, 15, 30);

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @Mock
    private Authentication authentication;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService);
        ReflectionTestUtils.setField(controller, "messageService", messageService);
        when(messageService.get(MessageCodeType.SUCCESS)).thenReturn(new MessageBo("0000", "Operación realizada con éxito"));
    }

    @Test
    void registroDelegaEnElServicioYMapeaLaRespuesta() {
        UUID id = UUID.randomUUID();
        when(userService.register(any(RegisterUserRequestBo.class))).thenReturn(RegisterUserResponseBo.builder()
                .id(id)
                .name("Juan Rodriguez")
                .email("juan@test.cl")
                .phones(List.of(phoneBo()))
                .created(NOW)
                .modified(NOW)
                .lastLogin(NOW)
                .token("jwt")
                .active(true)
                .build());

        BaseResponseDto<RegisterUserResponseDto> response = controller.register(requestDto());

        ArgumentCaptor<RegisterUserRequestBo> captor = ArgumentCaptor.forClass(RegisterUserRequestBo.class);
        verify(userService).register(captor.capture());
        RegisterUserRequestBo sent = captor.getValue();
        assertThat(sent.getName()).isEqualTo("Juan Rodriguez");
        assertThat(sent.getEmail()).isEqualTo("juan@test.cl");
        assertThat(sent.getPassword()).isEqualTo("Hunter22");
        assertThat(sent.getPhones()).singleElement().satisfies(phone -> {
            assertThat(phone.getCityCode()).isEqualTo("1");
            assertThat(phone.getCountryCode()).isEqualTo("57");
        });

        assertThat(response.getCode()).isEqualTo("0000");
        assertThat(response.getStatus()).isEqualTo(ResponseStatusType.SUCCESS);
        RegisterUserResponseDto data = response.getData();
        assertThat(data.getId()).isEqualTo(id);
        assertThat(data.getName()).isEqualTo("Juan Rodriguez");
        assertThat(data.getEmail()).isEqualTo("juan@test.cl");
        assertThat(data.getLastLogin()).isEqualTo(NOW);
        assertThat(data.getToken()).isEqualTo("jwt");
        assertThat(data.isActive()).isTrue();
        assertThat(data.getPhones()).singleElement().satisfies(phone -> assertThat(phone.getCountryCode()).isEqualTo("57"));
    }

    @Test
    void meConsultaAlUsuarioDelToken() {
        when(authentication.getName()).thenReturn("juan@test.cl");
        when(userService.getUserInfo(any(UserInfoRequestBo.class))).thenReturn(UserInfoResponseBo.builder()
                .id(UUID.randomUUID())
                .name("Juan Rodriguez")
                .email("juan@test.cl")
                .phones(List.of(phoneBo()))
                .created(NOW)
                .modified(NOW)
                .lastLogin(NOW)
                .active(true)
                .build());

        BaseResponseDto<UserInfoResponseDto> response = controller.me(authentication);

        ArgumentCaptor<UserInfoRequestBo> captor = ArgumentCaptor.forClass(UserInfoRequestBo.class);
        verify(userService).getUserInfo(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("juan@test.cl");
        assertThat(response.getCode()).isEqualTo("0000");
        assertThat(response.getData().getEmail()).isEqualTo("juan@test.cl");
        assertThat(response.getData().getPhones()).hasSize(1);
    }

    private RegisterUserRequestDto requestDto() {
        PhoneInfoDto phone = new PhoneInfoDto();
        phone.setNumber("1234567");
        phone.setCityCode("1");
        phone.setCountryCode("57");

        RegisterUserRequestDto request = new RegisterUserRequestDto();
        request.setName("Juan Rodriguez");
        request.setEmail("juan@test.cl");
        request.setPassword("Hunter22");
        request.setPhones(List.of(phone));
        return request;
    }

    private PhoneInfoBo phoneBo() {
        PhoneInfoBo phone = new PhoneInfoBo();
        phone.setNumber("1234567");
        phone.setCityCode("1");
        phone.setCountryCode("57");
        return phone;
    }
}
