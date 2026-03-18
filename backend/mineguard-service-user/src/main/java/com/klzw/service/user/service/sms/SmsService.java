package com.klzw.service.user.service.sms;

public interface SmsService {

    boolean sendSmsCode(String phone);

    boolean sendSmsVerificationCode(String phone, String templateId, String[] params);

    boolean sendLoginRegisterCode(String phone);

    boolean sendUpdatePhoneCode(String phone);

    boolean sendResetPasswordCode(String phone);

    boolean sendBindPhoneCode(String phone);

    boolean sendVerifyPhoneCode(String phone);

    boolean verifySmsCode(String phone, String code);

}
