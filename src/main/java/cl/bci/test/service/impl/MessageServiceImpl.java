package cl.bci.test.service.impl;

import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.persistence.model.MessageEntity;
import cl.bci.test.persistence.repository.MessageRepository;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.MessageBo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private Map<String, String> messages = Map.of();

    @PostConstruct
    void load() {
        messages = messageRepository.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(MessageEntity::getCode, MessageEntity::getMessage));

        List<String> missing = Arrays.stream(MessageCodeType.values())
                .map(MessageCodeType::getCode)
                .filter(c -> !messages.containsKey(c))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Códigos sin mensaje en la tabla messages: " + missing);
        }
    }

    @Override
    public MessageBo get(MessageCodeType code, Object... args) {
        String text = messages.get(code.getCode());
        return new MessageBo(code.getCode(), args.length == 0 ? text : text.formatted(args));
    }
}
