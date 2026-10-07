package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.MessageBo;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController {
    @Autowired
    private MessageService messageService;

    protected <T> BaseResponseDto<T> success(T data) {
        MessageBo msg = messageService.get(MessageCodeType.SUCCESS);
        return BaseResponseDto.success(msg.code(), msg.message(), data);
    }
}
