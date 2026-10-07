package cl.bci.test.controller.dto;

import cl.bci.test.validation.ValidPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterUserRequestDto {
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 100, message = "FIELD_TOO_LONG")
    private String name;
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 150, message = "FIELD_TOO_LONG")
    @Pattern(regexp = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$", message = "EMAIL_INVALID")
    private String email;
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 72, message = "FIELD_TOO_LONG")
    @ValidPassword
    private String password;
    @Valid
    private List<@NotNull(message = "FIELD_REQUIRED") PhoneInfoDto> phones;
}
