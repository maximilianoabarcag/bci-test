package cl.bci.test.service;

import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.service.bo.MessageBo;

public interface MessageService {
    MessageBo get(MessageCodeType code, Object... args);
}
