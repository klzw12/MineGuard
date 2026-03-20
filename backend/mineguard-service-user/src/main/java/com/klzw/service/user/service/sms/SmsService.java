package com.klzw.service.user.service.sms;

import com.klzw.service.user.enums.SmsScene;

public interface SmsService {

    boolean sendSmsCode(String phone);

    boolean sendSmsCode(String phone, SmsScene scene);

    boolean verifySmsCode(String phone, String code);

}
