package cl.bci.test.service;

public interface TokenService {
    String generateToken(String subject);
    String validateAndGetSubject(String token);
}
