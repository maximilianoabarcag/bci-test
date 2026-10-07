package cl.bci.test.service.mapper;

import cl.bci.test.persistence.model.PhoneEntity;
import cl.bci.test.persistence.model.UserEntity;
import cl.bci.test.service.bo.PhoneInfoBo;

import java.util.List;
import java.util.Locale;

public final class UserBoMapper {

    private UserBoMapper() {
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static PhoneEntity toPhoneEntity(PhoneInfoBo bo) {
        PhoneEntity phone = new PhoneEntity();
        phone.setNumber(bo.getNumber());
        phone.setCityCode(bo.getCityCode());
        phone.setCountryCode(bo.getCountryCode());
        return phone;
    }

    public static List<PhoneInfoBo> toPhoneInfoBos(UserEntity user) {
        return user.getPhones().stream()
                .map(UserBoMapper::toPhoneInfoBo)
                .toList();
    }

    private static PhoneInfoBo toPhoneInfoBo(PhoneEntity phone) {
        PhoneInfoBo bo = new PhoneInfoBo();
        bo.setNumber(phone.getNumber());
        bo.setCityCode(phone.getCityCode());
        bo.setCountryCode(phone.getCountryCode());
        return bo;
    }
}
