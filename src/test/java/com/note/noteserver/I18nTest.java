package com.note.noteserver;

import com.note.noteserver.util.I18nMessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 国际化功能测试
 */
@SpringBootTest
public class I18nTest {

    @Autowired
    private MessageSource messageSource;

    @Test
    void testMessageSourceLoaded() {
        assertNotNull(messageSource);
    }

    @Test
    void testChineseMessage() {
        String message = messageSource.getMessage("error.user.not.found", null, Locale.CHINA);
        assertEquals("用户不存在", message);
    }

    @Test
    void testEnglishMessage() {
        String message = messageSource.getMessage("error.user.not.found", null, Locale.ENGLISH);
        assertEquals("User not found", message);
    }

    @Test
    void testMessageWithArgs() {
        String message = messageSource.getMessage("error.sync.invalid.resolution", new Object[]{"test"}, Locale.CHINA);
        assertEquals("无效的解决方式: test", message);
    }

    @Test
    void testI18nMessageUtil() {
        // 设置当前语言环境为中文
        I18nMessageUtil.setLocale(Locale.CHINA);
        String cnMessage = I18nMessageUtil.getMessage("error.user.not.found");
        assertEquals("用户不存在", cnMessage);

        // 设置当前语言环境为英文
        I18nMessageUtil.setLocale(Locale.ENGLISH);
        String enMessage = I18nMessageUtil.getMessage("error.user.not.found");
        assertEquals("User not found", enMessage);
    }

    @Test
    void testSuccessMessages() {
        String cnLogout = messageSource.getMessage("success.logout", null, Locale.CHINA);
        assertEquals("登出成功", cnLogout);

        String enLogout = messageSource.getMessage("success.logout", null, Locale.ENGLISH);
        assertEquals("Logout successful", enLogout);
    }
}
