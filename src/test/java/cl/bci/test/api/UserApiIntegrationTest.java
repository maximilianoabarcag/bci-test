package cl.bci.test.api;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserApiIntegrationTest {

    private static final String USERS = "/api/v1/users";
    private static final String ME = "/api/v1/users/me";
    private static final String LOGIN = "/api/v1/auth/login";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void registraUsuarioYRetornaSusDatosConElToken() throws Exception {
        String email = uniqueEmail();

        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(user(email, "Hunter22")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("Juan Rodriguez"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.phones[0].citycode").value("1"))
                .andExpect(jsonPath("$.data.phones[0].contrycode").value("57"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.isactive").value(true))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void rechazaCorreoDuplicado() throws Exception {
        String email = uniqueEmail();
        register(email);

        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(user(email.toUpperCase(), "Hunter22")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("0601"))
                .andExpect(jsonPath("$.mensaje").value("El correo ya está registrado"))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void rechazaCorreoConFormatoInvalido() throws Exception {
        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(user("juan@", "Hunter22")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("0402"));
    }

    @Test
    void rechazaContrasenaConFormatoInvalido() throws Exception {
        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(user(uniqueEmail(), "hunter2")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("0403"));
    }

    @Test
    void rechazaCampoQueExcedeElLargoMaximo() throws Exception {
        String body = """
                {"name": "%s", "email": "%s", "password": "Hunter22"}
                """.formatted("J".repeat(101), uniqueEmail());

        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("0405"));
    }

    @Test
    void rechazaJsonMalFormado() throws Exception {
        mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("0404"));
    }

    @Test
    void rechazaTipoDeContenidoNoSoportado() throws Exception {
        mvc.perform(post(USERS).contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("0407"));
    }

    @Test
    void consultaElUsuarioAutenticado() throws Exception {
        String email = uniqueEmail();
        String token = register(email);

        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.token").doesNotExist());
    }

    @Test
    void rechazaMeSinToken() throws Exception {
        mvc.perform(get(ME))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("0501"));
    }

    @Test
    void rechazaMeConTokenInvalido() throws Exception {
        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("0502"));
    }

    @Test
    void loginEmiteTokenNuevoEInvalidaElAnterior() throws Exception {
        String email = uniqueEmail();
        String oldToken = register(email);

        MvcResult result = mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(login(email.toUpperCase(), "Hunter22")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andReturn();
        String newToken = JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");

        assertThat(newToken).isNotEqualTo(oldToken);
        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isOk());
        mvc.perform(get(ME).header(HttpHeaders.AUTHORIZATION, "Bearer " + oldToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("0501"));
    }

    @Test
    void loginRechazaCredencialesIncorrectas() throws Exception {
        String email = uniqueEmail();
        register(email);

        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(login(email, "Otra1234")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("0503"));
        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(login(uniqueEmail(), "Hunter22")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("0503"));
    }

    @Test
    void rechazaMetodoNoPermitido() throws Exception {
        String token = register(uniqueEmail());

        mvc.perform(put(USERS).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("0406"));
    }

    @Test
    void rechazaRutaInexistente() throws Exception {
        String token = register(uniqueEmail());

        mvc.perform(get("/api/v1/no-existe").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("0701"));
    }

    @Test
    void healthEsPublicoYReportaElEstado() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void infoEsPublicoYExponeLosDatosDeLaAplicacion() throws Exception {
        mvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("bci-test"))
                .andExpect(jsonPath("$.java.version").isNotEmpty());
    }

    @Test
    void otrosEndpointsDeActuatorNoEstanExpuestos() throws Exception {
        String token = register(uniqueEmail());

        mvc.perform(get("/actuator/env").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String register(String email) throws Exception {
        MvcResult result = mvc.perform(post(USERS).contentType(MediaType.APPLICATION_JSON).content(user(email, "Hunter22")))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");
    }

    private String uniqueEmail() {
        return "user." + UUID.randomUUID() + "@test.cl";
    }

    private String user(String email, String password) {
        return """
                {
                  "name": "Juan Rodriguez",
                  "email": "%s",
                  "password": "%s",
                  "phones": [{"number": "1234567", "citycode": "1", "contrycode": "57"}]
                }
                """.formatted(email, password);
    }

    private String login(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }
}
