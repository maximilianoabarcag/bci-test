package cl.bci.test.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 150, message = "FIELD_TOO_LONG")
    private String email;
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 72, message = "FIELD_TOO_LONG")
    private String password;
}
