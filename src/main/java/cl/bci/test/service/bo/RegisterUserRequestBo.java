package cl.bci.test.service.bo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegisterUserRequestBo {
    private String name;
    private String email;
    private String password;
    private List<PhoneInfoBo> phones;
}
