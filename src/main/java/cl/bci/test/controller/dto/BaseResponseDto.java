package cl.bci.test.controller.dto;

import cl.bci.test.enums.ResponseStatusType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponseDto<T> {
    private String code;
    @JsonProperty("mensaje")
    private String message;
    private ResponseStatusType status;

    private T data;

    public static <T> BaseResponseDto<T> success(String code, String message, T data) {
        return BaseResponseDto.<T>builder()
                .status(ResponseStatusType.SUCCESS)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> BaseResponseDto<T> error(String code, String message) {
        return BaseResponseDto.<T>builder()
                .status(ResponseStatusType.ERROR)
                .code(code)
                .message(message)
                .build();
    }
}
