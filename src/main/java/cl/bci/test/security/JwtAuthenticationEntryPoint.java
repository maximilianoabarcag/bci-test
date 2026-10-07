package cl.bci.test.security;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.MessageBo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageService messageService;
    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        MessageCodeType code = request.getAttribute(JwtAuthenticationFilter.SECURITY_ERROR_CODE) instanceof MessageCodeType c
                ? c
                : MessageCodeType.TOKEN_INVALID;
        MessageBo msg = messageService.get(code);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                jsonMapper.writeValueAsString(BaseResponseDto.error(msg.code(), msg.message())));
    }
}
