package cl.bci.test.service;

import cl.bci.test.service.bo.RegisterUserRequestBo;
import cl.bci.test.service.bo.RegisterUserResponseBo;
import cl.bci.test.service.bo.UserInfoRequestBo;
import cl.bci.test.service.bo.UserInfoResponseBo;

public interface UserService {

    RegisterUserResponseBo register(RegisterUserRequestBo registerUserRequest);

    UserInfoResponseBo getUserInfo(UserInfoRequestBo userInfoRequest);
}
