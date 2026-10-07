package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.controller.dto.RegisterUserRequestDto;
import cl.bci.test.controller.dto.RegisterUserResponseDto;
import cl.bci.test.controller.dto.UserInfoResponseDto;
import cl.bci.test.domain.Endpoint;
import cl.bci.test.service.UserService;
import cl.bci.test.service.bo.RegisterUserRequestBo;
import cl.bci.test.service.bo.RegisterUserResponseBo;
import cl.bci.test.service.bo.UserInfoRequestBo;
import cl.bci.test.service.bo.UserInfoResponseBo;
import cl.bci.test.util.MapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios", description = "Registro y consulta de usuarios")
@RestController
@RequiredArgsConstructor
@RequestMapping(Endpoint.API_V1 + "/users")
public class UserController extends BaseController {

    private final UserService userService;

    @Operation(summary = "Registrar usuario",
            description = "Crea un usuario con sus teléfonos y retorna sus datos junto al token de acceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado (0000)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (0401, 0402, 0403, 0404, 0405)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "El correo ya está registrado (0601)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class))),
            @ApiResponse(responseCode = "415", description = "Tipo de contenido no soportado (0407)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class))),
            @ApiResponse(responseCode = "503", description = "Servicio no disponible (9999)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponseDto<RegisterUserResponseDto> register(@Valid @RequestBody RegisterUserRequestDto request) {
        RegisterUserResponseBo user =
                userService.register(MapperUtil.map(request, RegisterUserRequestBo.class));

        return success(MapperUtil.map(user, RegisterUserResponseDto.class));
    }

    @Operation(summary = "Consultar el usuario autenticado",
            description = "Retorna la información del usuario dueño del token enviado en el header Authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Información del usuario (0000)"),
            @ApiResponse(responseCode = "401", description = "Token ausente, no vigente (0501), inválido o expirado (0502)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class))),
            @ApiResponse(responseCode = "503", description = "Servicio no disponible (9999)",
                    content = @Content(schema = @Schema(implementation = BaseResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseDto<UserInfoResponseDto> me(Authentication authentication) {
        UserInfoResponseBo user = userService.getUserInfo(
                UserInfoRequestBo.builder().email(authentication.getName()).build());

        return success(MapperUtil.map(user, UserInfoResponseDto.class));
    }
}
