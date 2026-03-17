package com.note.noteserver.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息工具类
 * 提供静态方法获取国际化消息
 */
@Slf4j
@Component
public class I18nMessageUtil {

    private static MessageSource staticMessageSource;

    @Autowired
    private MessageSource messageSource;

    @PostConstruct
    public void init() {
        staticMessageSource = messageSource;
        log.info("I18nMessageUtil initialized");
    }

    /**
     * 获取国际化消息（使用当前线程的语言环境）
     *
     * @param code 消息代码
     * @return 国际化消息
     */
    public static String getMessage(String code) {
        return getMessage(code, (Object[]) null);
    }

    /**
     * 获取国际化消息（使用当前线程的语言环境）
     *
     * @param code 消息代码
     * @param args 消息参数
     * @return 国际化消息
     */
    public static String getMessage(String code, Object... args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 获取国际化消息（指定语言环境）
     *
     * @param code   消息代码
     * @param locale 语言环境
     * @return 国际化消息
     */
    public static String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    /**
     * 获取国际化消息（指定语言环境和参数）
     *
     * @param code   消息代码
     * @param args   消息参数
     * @param locale 语言环境
     * @return 国际化消息
     */
    public static String getMessage(String code, Object[] args, Locale locale) {
        if (staticMessageSource == null) {
            log.warn("MessageSource not initialized, returning code: {}", code);
            return code;
        }
        try {
            return staticMessageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            log.warn("Message not found for code: {}, locale: {}", code, locale);
            return code;
        }
    }

    /**
     * 获取当前语言环境
     *
     * @return 当前语言环境
     */
    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * 设置当前语言环境
     *
     * @param locale 语言环境
     */
    public static void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }

    /**
     * 获取默认语言环境
     *
     * @return 默认语言环境
     */
    public static Locale getDefaultLocale() {
        return Locale.CHINA;
    }
}
