package cl.bci.test.exception;

import cl.bci.test.enums.MessageCodeType;
import lombok.Getter;

@Getter
public class SecurityException extends java.lang.SecurityException {

    private final MessageCodeType messageCode;

    public SecurityException(MessageCodeType messageCode) {
        super(messageCode.name());
        this.messageCode = messageCode;
    }

    public SecurityException(MessageCodeType messageCode, Throwable cause) {
        super(messageCode.name(), cause);
        this.messageCode = messageCode;
    }
}
