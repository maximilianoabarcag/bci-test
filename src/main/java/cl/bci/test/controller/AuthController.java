package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.controller.dto.LoginRequestDto;
import cl.bci.test.controller.dto.LoginResponseDto;
import cl.bci.test.domain.Endpoint;
import cl.bci.test.service.AuthService;
import cl.bci.test.service.bo.LoginRequestBo;
import cl.bci.test.service.bo.LoginResponseBo;
import cl.bci.test.util.MapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación", description = "Inicio de sesión y renovación del token")
@RestController
@RequiredArgsConstructor
@RequestMapping(Endpoint.API_V1 + "/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión",
            description = "Valida las credenciales, emite un token nuevo y actualiza last_login. El token anterior deja de ser válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso (0000)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (0401, 0404, 0405)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos (0503)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class))),
            @ApiResponse(responseCode = "503", description = "Servicio no disponible (9999)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class)))
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseDto<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseBo session = authService.login(MapperUtil.map(request, LoginRequestBo.class));

        return success(MapperUtil.map(session, LoginResponseDto.class));
    }
}
