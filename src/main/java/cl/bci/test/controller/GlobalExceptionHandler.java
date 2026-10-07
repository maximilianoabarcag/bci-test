package cl.bci.test.controller;

import cl.bci.test.controller.dto.BaseResponseDto;
import cl.bci.test.enums.MessageCodeType;
import cl.bci.test.exception.BusinessException;
import cl.bci.test.exception.SecurityException;
import cl.bci.test.service.MessageService;
import cl.bci.test.service.bo.MessageBo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String EMAIL_UNIQUE_CONSTRAINT = "UK_USERS_EMAIL";

    private final MessageService messageService;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        MessageBo msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> messageService.get(toMessageCode(fe.getDefaultMessage()), fe.getField()))
                .orElseGet(() -> messageService.get(MessageCodeType.VALIDATION_ERROR));
        return toResponse(msg, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return toResponse(messageService.get(MessageCodeType.MALFORMED_REQUEST), headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Error de la plataforma", ex);
            return toResponse(messageService.get(MessageCodeType.INTERNAL_ERROR), headers, HttpStatus.SERVICE_UNAVAILABLE);
        }

        MessageCodeType code = switch (ex) {
            case HttpRequestMethodNotSupportedException e -> MessageCodeType.METHOD_NOT_ALLOWED;
            case HttpMediaTypeNotSupportedException e -> MessageCodeType.MEDIA_TYPE_NOT_SUPPORTED;
            case NoResourceFoundException e -> MessageCodeType.NOT_FOUND;
            default -> MessageCodeType.VALIDATION_ERROR;
        };
        log.warn("Solicitud rechazada: {}", ex.getMessage());
        return toResponse(messageService.get(code), headers, statusCode);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleBusiness(BusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessageCode());
        return build(messageService.get(ex.getMessageCode(), ex.getArgs()), statusFor(ex.getMessageCode()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleSecurity(SecurityException ex) {
        log.warn("Error de seguridad: {}", ex.getMessageCode());
        return build(messageService.get(ex.getMessageCode()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String cause = String.valueOf(ex.getMostSpecificCause().getMessage()).toUpperCase();
        log.warn("Violación de integridad: {}", cause);
        MessageCodeType code = cause.contains(EMAIL_UNIQUE_CONSTRAINT)
                ? MessageCodeType.EMAIL_ALREADY_EXISTS
                : MessageCodeType.DUPLICATE;
        return build(messageService.get(code), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<BaseResponseDto<Void>> handleNotFound(NoSuchElementException ex) {
        return build(messageService.get(MessageCodeType.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponseDto<Void>> handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        return build(messageService.get(MessageCodeType.INTERNAL_ERROR), HttpStatus.SERVICE_UNAVAILABLE);
    }

    private HttpStatus statusFor(MessageCodeType code) {
        return switch (code.getCode().substring(0, 2)) {
            case "04" -> HttpStatus.BAD_REQUEST;
            case "05" -> HttpStatus.UNAUTHORIZED;
            case "06" -> HttpStatus.CONFLICT;
            case "07" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }

    private MessageCodeType toMessageCode(String name) {
        try {
            return MessageCodeType.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return MessageCodeType.VALIDATION_ERROR;
        }
    }

    private ResponseEntity<Object> toResponse(MessageBo msg, HttpHeaders headers, HttpStatusCode status) {
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BaseResponseDto.error(msg.code(), msg.message()));
    }

    private ResponseEntity<BaseResponseDto<Void>> build(MessageBo msg, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(BaseResponseDto.error(msg.code(), msg.message()));
    }
}
