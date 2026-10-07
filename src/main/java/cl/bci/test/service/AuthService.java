package cl.bci.test.service;

import cl.bci.test.service.bo.LoginRequestBo;
import cl.bci.test.service.bo.LoginResponseBo;

public interface AuthService {

    LoginResponseBo login(LoginRequestBo loginRequest);
}
