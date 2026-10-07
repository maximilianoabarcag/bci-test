package cl.bci.test.service.bo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PhoneInfoBo {
    private String number;
    private String cityCode;
    private String countryCode;
}
