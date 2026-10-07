package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.enums.ResponseStatusType;
import cl.bci.test.exception.EmailAlreadyExistsException;
import cl.bci.test.exception.InvalidCredentialsException;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.MessageBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void validacionDelDtoRespondeElCodigoDeLaAnotacionCon400() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "EMAIL_INVALID"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);
        stub(MessageCodeType.EMAIL_INVALID, "email");

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        assertError(response, HttpStatus.BAD_REQUEST, "0402");
    }

    @Test
    void validacionSinCodigoConocidoRespondeErrorGenerico() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "no debe estar vacío"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);
        stub(MessageCodeType.VALIDATION_ERROR, "name");

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        assertError(response, HttpStatus.BAD_REQUEST, "0400");
    }

    @Test
    void jsonMalFormadoResponde400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON inválido", new MockHttpInputMessage(new byte[0]));
        stub(MessageCodeType.MALFORMED_REQUEST);

        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        assertError(response, HttpStatus.BAD_REQUEST, "0404");
    }

    @Test
    void metodoNoPermitidoResponde405() {
        stub(MessageCodeType.METHOD_NOT_ALLOWED);

        ResponseEntity<Object> response = handler.handleExceptionInternal(
                new HttpRequestMethodNotSupportedException("PUT"), null, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, webRequest);

        assertError(response, HttpStatus.METHOD_NOT_ALLOWED, "0406");
    }

    @Test
    void tipoDeContenidoNoSoportadoResponde415() {
        stub(MessageCodeType.MEDIA_TYPE_NOT_SUPPORTED);

        ResponseEntity<Object> response = handler.handleExceptionInternal(
                new HttpMediaTypeNotSupportedException("text/plain"), null, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE, webRequest);

        assertError(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "0407");
    }

    @Test
    void errorDelFrameworkConStatus5xxResponde503() {
        stub(MessageCodeType.INTERNAL_ERROR);

        ResponseEntity<Object> response = handler.handleExceptionInternal(
                new IllegalStateException("falla"), null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, webRequest);

        assertError(response, HttpStatus.SERVICE_UNAVAILABLE, "9999");
    }

    @Test
    void correoDuplicadoResponde409() {
        stub(MessageCodeType.EMAIL_ALREADY_EXISTS);

        assertError(handler.handleBusiness(new EmailAlreadyExistsException()), HttpStatus.CONFLICT, "0601");
    }

    @Test
    void credencialesInvalidasResponden401() {
        stub(MessageCodeType.INVALID_CREDENTIALS);

        assertError(handler.handleSecurity(new InvalidCredentialsException()), HttpStatus.UNAUTHORIZED, "0503");
    }

    @Test
    void violacionDeLaRestriccionDelCorreoResponde409ConCorreoDuplicado() {
        stub(MessageCodeType.EMAIL_ALREADY_EXISTS);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicado",
                new RuntimeException("Unique index or primary key violation: \"PUBLIC.UK_USERS_EMAIL_INDEX_4 ON PUBLIC.USERS(EMAIL)\""));

        assertError(handler.handleDataIntegrity(ex), HttpStatus.CONFLICT, "0601");
    }

    @Test
    void otraViolacionDeIntegridadResponde409Generico() {
        stub(MessageCodeType.DUPLICATE);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicado",
                new RuntimeException("Unique index or primary key violation: \"PUBLIC.CONSTRAINT_INDEX_9 ON PUBLIC.USERS(UUID)\""));

        assertError(handler.handleDataIntegrity(ex), HttpStatus.CONFLICT, "0600");
    }

    @Test
    void recursoNoEncontradoResponde404() {
        stub(MessageCodeType.NOT_FOUND);

        assertError(handler.handleNotFound(new NoSuchElementException()), HttpStatus.NOT_FOUND, "0701");
    }

    @Test
    void errorInesperadoResponde503() {
        stub(MessageCodeType.INTERNAL_ERROR);

        assertError(handler.handleGeneric(new RuntimeException("falla")), HttpStatus.SERVICE_UNAVAILABLE, "9999");
    }

    private void stub(MessageCodeType code, Object... args) {
        when(messageService.get(code, args)).thenReturn(new MessageBo(code.getCode(), "Mensaje " + code.getCode()));
    }

    private void assertError(ResponseEntity<?> response, HttpStatus status, String code) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(BaseResponseDto.class);
        BaseResponseDto<?> body = (BaseResponseDto<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo(code);
        assertThat(body.getMessage()).isEqualTo("Mensaje " + code);
        assertThat(body.getStatus()).isEqualTo(ResponseStatusType.ERROR);
        assertThat(body.getData()).isNull();
    }
}
