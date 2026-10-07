package cl.bci.test.service.impl;

import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.persistence.model.MessageEntity;
import cl.bci.test.persistence.repository.MessageRepository;
import cl.bci.test.service.bo.MessageBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void cargaLosMensajesYLosEntregaPorCodigo() {
        List<MessageEntity> all = Arrays.stream(MessageCodeType.values())
                .map(code -> entity(code.getCode(), "Mensaje " + code.getCode()))
                .toList();
        when(messageRepository.findAll()).thenReturn(all);

        messageService.load();
        MessageBo msg = messageService.get(MessageCodeType.EMAIL_ALREADY_EXISTS);

        assertThat(msg.code()).isEqualTo("0601");
        assertThat(msg.message()).isEqualTo("Mensaje 0601");
        verify(messageRepository).findAll();
    }

    @Test
    void reemplazaLosArgumentosDelMensaje() {
        List<MessageEntity> all = Arrays.stream(MessageCodeType.values())
                .map(code -> entity(code.getCode(), code == MessageCodeType.FIELD_REQUIRED ? "El campo %s es obligatorio" : "Mensaje"))
                .toList();
        when(messageRepository.findAll()).thenReturn(all);

        messageService.load();

        assertThat(messageService.get(MessageCodeType.FIELD_REQUIRED, "name").message())
                .isEqualTo("El campo name es obligatorio");
    }

    @Test
    void fallaAlIniciarSiFaltanCodigosEnLaTabla() {
        when(messageRepository.findAll()).thenReturn(List.of(entity("0000", "Operación realizada con éxito")));

        assertThatThrownBy(() -> messageService.load())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("0601")
                .hasMessageContaining("9999");
    }

    private MessageEntity entity(String code, String message) {
        MessageEntity entity = new MessageEntity();
        ReflectionTestUtils.setField(entity, "code", code);
        ReflectionTestUtils.setField(entity, "message", message);
        return entity;
    }
}
