package cl.bci.test.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageCodeType {
    SUCCESS("0000"),
    VALIDATION_ERROR("0400"),
    FIELD_REQUIRED("0401"),
    EMAIL_INVALID("0402"),
    PASSWORD_INVALID("0403"),
    MALFORMED_REQUEST("0404"),
    FIELD_TOO_LONG("0405"),
    METHOD_NOT_ALLOWED("0406"),
    MEDIA_TYPE_NOT_SUPPORTED("0407"),
    TOKEN_INVALID("0501"),
    INVALID_TOKEN("0502"),
    INVALID_CREDENTIALS("0503"),
    DUPLICATE("0600"),
    EMAIL_ALREADY_EXISTS("0601"),
    NOT_FOUND("0701"),
    INTERNAL_ERROR("9999");

    private final String code;
}
