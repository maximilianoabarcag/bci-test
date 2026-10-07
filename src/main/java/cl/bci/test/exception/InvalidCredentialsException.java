package cl.bci.test.exception;

import cl.bci.test.enums.MessageCodeType;

public class InvalidCredentialsException extends SecurityException {

    public InvalidCredentialsException() {
        super(MessageCodeType.INVALID_CREDENTIALS);
    }
}
