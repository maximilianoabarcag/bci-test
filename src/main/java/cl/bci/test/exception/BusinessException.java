package cl.bci.test.exception;

import cl.bci.test.enums.MessageCodeType;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final MessageCodeType messageCode;
    private final transient Object[] args;

    public BusinessException(MessageCodeType messageCode, Object... args) {
        super(messageCode.name());
        this.messageCode = messageCode;
        this.args = args;
    }
}
