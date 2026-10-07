package cl.bci.test.exception;

import cl.bci.test.enums.MessageCodeType;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super(MessageCodeType.EMAIL_ALREADY_EXISTS);
    }
}
