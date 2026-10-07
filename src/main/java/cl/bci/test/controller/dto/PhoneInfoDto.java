package cl.bci.test.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneInfoDto {
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 20, message = "FIELD_TOO_LONG")
    private String number;
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 10, message = "FIELD_TOO_LONG")
    @JsonProperty("citycode")
    private String cityCode;
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(max = 10, message = "FIELD_TOO_LONG")
    @JsonProperty("contrycode")
    private String countryCode;
}
