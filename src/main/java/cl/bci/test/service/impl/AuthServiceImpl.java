package cl.bci.test.service.impl;

import cl.bci.test.exception.InvalidCredentialsException;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.persistence.repository.UserRepository;
import cl.bci.test.service.AuthService;
import cl.bci.test.service.TokenService;
import cl.bci.test.service.bo.LoginRequestBo;
import cl.bci.test.service.bo.LoginResponseBo;
import cl.bci.test.service.mapper.UserBoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @Transactional
    public LoginResponseBo login(LoginRequestBo loginRequest) {
        String email = UserBoMapper.normalizeEmail(loginRequest.getEmail());

        UserEntity user = userRepository.findByEmail(email)
                .filter(UserEntity::isActive)
                .filter(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);

        user.setToken(tokenService.generateToken(email));
        user.setLastLogin(LocalDateTime.now());

        UserEntity saved = userRepository.saveAndFlush(user);
        log.info("Inicio de sesión uuid={}", saved.getUuid());

        return LoginResponseBo.builder()
                .id(saved.getUuid())
                .name(saved.getName())
                .email(saved.getEmail())
                .phones(UserBoMapper.toPhoneInfoBos(saved))
                .created(saved.getCreatedAt())
                .modified(saved.getUpdatedAt())
                .lastLogin(saved.getLastLogin())
                .token(saved.getToken())
                .active(saved.isActive())
                .build();
    }
}
