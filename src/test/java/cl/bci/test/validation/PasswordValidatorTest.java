package cl.bci.test.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

    @Mock
    private ConstraintValidatorContext context;

    @ParameterizedTest
    @ValueSource(strings = {"Hunter22", "Clave123", "Bci2026Test", "Hunter22!@#$%"})
    void aceptaContrasenasQueCumplenElFormato(String password) {
        assertThat(validator.isValid(password, context)).isTrue();
        verifyNoInteractions(context);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hunter2", "hunter22", "HUNTER22", "Hunterrr", "Hunte22", "12345678"})
    void rechazaContrasenasQueNoCumplenElFormato(String password) {
        assertThat(validator.isValid(password, context)).isFalse();
        verifyNoInteractions(context);
    }

    @Test
    void delegaLosValoresVaciosEnNotBlank() {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("   ", context)).isTrue();
        verifyNoInteractions(context);
    }
}
