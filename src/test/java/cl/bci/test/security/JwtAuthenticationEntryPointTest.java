package cl.bci.test.security;

import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.MessageBo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private MessageService messageService;

    private JwtAuthenticationEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint(messageService, jsonMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void respondeTokenAusenteONoVigentePorDefecto() throws Exception {
        when(messageService.get(MessageCodeType.TOKEN_INVALID))
                .thenReturn(new MessageBo("0501", "Token inválido, expirado o ausente"));

        entryPoint.commence(request, response, new InsufficientAuthenticationException("sin token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(body())
                .contains("\"code\":\"0501\"")
                .contains("\"mensaje\":\"Token inválido, expirado o ausente\"")
                .contains("\"status\":\"ERROR\"");
    }

    @Test
    void respondeElCodigoMarcadoPorElFiltro() throws Exception {
        request.setAttribute(JwtAuthenticationFilter.SECURITY_ERROR_CODE, MessageCodeType.INVALID_TOKEN);
        when(messageService.get(MessageCodeType.INVALID_TOKEN))
                .thenReturn(new MessageBo("0502", "El token es inválido o ha expirado"));

        entryPoint.commence(request, response, new InsufficientAuthenticationException("token inválido"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body()).contains("\"code\":\"0502\"");
    }

    private String body() throws Exception {
        return response.getContentAsString(StandardCharsets.UTF_8);
    }
}
