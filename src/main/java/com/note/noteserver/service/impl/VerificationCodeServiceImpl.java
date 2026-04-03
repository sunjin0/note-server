package com.note.noteserver.service.impl;

import com.note.noteserver.config.AppMailProperties;
import com.note.noteserver.config.VerificationCodeProperties;
import com.note.noteserver.exception.ServiceException;
import com.note.noteserver.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final JavaMailSender mailSender;
    private final VerificationCodeProperties properties;
    private final AppMailProperties mailProperties;

    private final SecureRandom random = new SecureRandom();

    /**
     * 内存验证码存储：适用于单机部署。
     * 如果是多实例，需要替换为 Redis/DB。
     */
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void sendEmailCode(String email, String purpose) {
        String key = key(email, purpose);
        Instant now = Instant.now();

        Entry existing = store.get(key);
        if (existing != null) {
            existing = existing.cleanIfExpired(now);
        }
        if (existing != null && existing.lastSentAt != null) {
            long secondsSinceLast = Duration.between(existing.lastSentAt, now).getSeconds();
            if (secondsSinceLast < properties.getResendCooldownSeconds()) {
                throw new ServiceException("error.code.send.too.frequently");
            }
        }

        String code = properties.isDevMode() ? properties.getDevCode() : generate6Digits();
        Entry entry = new Entry(code, now.plusSeconds(properties.getTtlSeconds()), now, 0);
        store.put(key, entry);

        if (properties.isDevMode()) {
            log.info("[DEV MODE] Email code for purpose={}, email={}, code={}", purpose, maskEmail(email), code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(email);
        message.setSubject(mailProperties.getSubjectPrefix() + subjectForPurpose(purpose));
        message.setText("Your verification code is: " + code + "\n\nIt will expire in " + properties.getTtlSeconds() + " seconds.");
        mailSender.send(message);

        log.info("Sent email code, purpose={}, email={}", purpose, maskEmail(email));
    }

    @Override
    public void verifyEmailCodeOrThrow(String email, String purpose, String code) {
        String key = key(email, purpose);
        Instant now = Instant.now();
        Entry entry = store.get(key);
        if (entry == null) {
            throw new ServiceException("error.code.invalid");
        }

        entry = entry.cleanIfExpired(now);
        if (entry == null) {
            store.remove(key);
            throw new ServiceException("error.code.expired");
        }

        if (entry.attempts >= properties.getMaxAttempts()) {
            store.remove(key);
            throw new ServiceException("error.code.too.many.attempts");
        }

        boolean valid = Objects.equals(entry.code, code) || 
                        (properties.isDevMode() && Objects.equals(properties.getDevCode(), code));
        if (!valid) {
            store.computeIfPresent(key, (k, v) -> v.withAttempts(v.attempts + 1));
            throw new ServiceException("error.code.invalid");
        }

        // consume
        store.remove(key);
    }

    private String generate6Digits() {
        int n = random.nextInt(1_000_000);
        return String.format(Locale.ROOT, "%06d", n);
    }

    private String key(String email, String purpose) {
        return (email == null ? "" : email.trim().toLowerCase(Locale.ROOT)) + "::" + (purpose == null ? "" : purpose.trim().toLowerCase(Locale.ROOT));
    }

    private String subjectForPurpose(String purpose) {
        if (purpose == null) return "Verification Code";
        String p = purpose.trim().toLowerCase(Locale.ROOT);
        return switch (p) {
            case "forgot_password", "forgot-password" -> "Password Reset";
            default -> "Verification Code";
        };
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    private record Entry(String code, Instant expiresAt, Instant lastSentAt, int attempts) {
        Entry cleanIfExpired(Instant now) {
            if (expiresAt == null || now == null) return this;
            return now.isAfter(expiresAt) ? null : this;
        }

        Entry withAttempts(int attempts) {
            return new Entry(code, expiresAt, lastSentAt, attempts);
        }
    }
}
