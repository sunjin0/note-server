package com.note.noteserver.service;

/**
 * 通用验证码服务
 */
public interface VerificationCodeService {

    /**
     * 生成并发送邮箱验证码（同一邮箱+purpose 会受频率限制）
     */
    void sendEmailCode(String email, String purpose);

    /**
     * 校验邮箱验证码。校验成功后会消费该验证码。
     */
    void verifyEmailCodeOrThrow(String email, String purpose, String code);
}
