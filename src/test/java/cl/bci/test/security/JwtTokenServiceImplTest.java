package cl.bci.test.security;

import cl.bci.test.exception.InvalidTokenException;
import cl.bci.test.enums.MessageCodeType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceImplTest {

    private static final String SECRET = "WKFLKkvbVrezKZLXqU0sUPrXbTeajn9/Cjidypm89fk=";
    private static final String OTHER_SECRET = "c2VjcmV0by1kaXN0aW50by1wYXJhLXByb2JhcnRva2Vucw==";

    private final JwtTokenServiceImpl tokenService = new JwtTokenServiceImpl(SECRET, 3_600_000);

    @Test
    void generaUnTokenQueSeValidaConElMismoSubject() {
        String token = tokenService.generateToken("juan@test.cl");

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(tokenService.validateAndGetSubject(token)).isEqualTo("juan@test.cl");
    }

    @Test
    void cadaTokenEsUnico() {
        assertThat(tokenService.generateToken("juan@test.cl"))
                .isNotEqualTo(tokenService.generateToken("juan@test.cl"));
    }

    @Test
    void rechazaUnTokenMalFormado() {
        assertThatThrownBy(() -> tokenService.validateAndGetSubject("abc.def.ghi"))
                .isInstanceOf(InvalidTokenException.class)
                .extracting("messageCode").isEqualTo(MessageCodeType.INVALID_TOKEN);
    }

    @Test
    void rechazaUnTokenVacio() {
        assertThatThrownBy(() -> tokenService.validateAndGetSubject(""))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        String token = new JwtTokenServiceImpl(OTHER_SECRET, 3_600_000).generateToken("juan@test.cl");

        assertThatThrownBy(() -> tokenService.validateAndGetSubject(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaUnTokenExpirado() {
        String token = new JwtTokenServiceImpl(SECRET, -1_000).generateToken("juan@test.cl");

        assertThatThrownBy(() -> tokenService.validateAndGetSubject(token))
                .isInstanceOf(InvalidTokenException.class);
    }
}
