package cl.bci.test.service.impl;

import cl.bci.test.exception.EmailAlreadyExistsException;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.persistence.repository.UserRepository;
import cl.bci.test.service.TokenService;
import cl.bci.test.service.UserService;
import cl.bci.test.service.bo.RegisterUserRequestBo;
import cl.bci.test.service.bo.RegisterUserResponseBo;
import cl.bci.test.service.bo.UserInfoRequestBo;
import cl.bci.test.service.bo.UserInfoResponseBo;
import cl.bci.test.service.mapper.UserBoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @Transactional
    public RegisterUserResponseBo register(RegisterUserRequestBo registerUserRequest) {
        String email = UserBoMapper.normalizeEmail(registerUserRequest.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        UserEntity user = new UserEntity();
        user.setName(registerUserRequest.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setToken(tokenService.generateToken(email));

        Optional.ofNullable(registerUserRequest.getPhones())
                .orElse(List.of())
                .forEach(phone -> user.addPhone(UserBoMapper.toPhoneEntity(phone)));

        UserEntity saved = userRepository.saveAndFlush(user);
        log.info("Usuario registrado uuid={}", saved.getUuid());

        return RegisterUserResponseBo.builder()
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

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseBo getUserInfo(UserInfoRequestBo userInfoRequest) {
        UserEntity user = userRepository.findByEmail(userInfoRequest.getEmail()).orElseThrow();

        return UserInfoResponseBo.builder()
                .id(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .phones(UserBoMapper.toPhoneInfoBos(user))
                .created(user.getCreatedAt())
                .modified(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .active(user.isActive())
                .build();
    }
}
