package cl.bci.test.exception;

import cl.bci.test.enums.MessageCodeType;

public class InvalidTokenException extends SecurityException {

    public InvalidTokenException(Throwable cause) {
        super(MessageCodeType.INVALID_TOKEN, cause);
    }
}
