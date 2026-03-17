package com.note.noteserver.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 国际化配置类
 * 配置 MessageSource 和 LocaleResolver
 */
@Configuration
public class MessageSourceConfig {

    /**
     * 支持的语言列表
     */
    public static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            Locale.CHINA,           // zh_CN
            Locale.ENGLISH,         // en
            Locale.US              // en_US
    );

    /**
     * 默认语言
     */
    public static final Locale DEFAULT_LOCALE = Locale.CHINA;

    /**
     * 配置 MessageSource
     * 用于加载国际化资源文件
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // 设置资源文件基础名称（相对于 classpath）
        messageSource.setBasename("classpath:i18n/messages");
        // 设置编码
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        // 设置缓存时间（秒）
        messageSource.setCacheSeconds(3600);
        // 当找不到对应语言的资源时，使用默认语言
        messageSource.setFallbackToSystemLocale(false);
        // 设置默认语言
        messageSource.setDefaultLocale(DEFAULT_LOCALE);
        // 当找不到消息时，使用消息代码作为返回值
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * 配置 LocaleResolver
     * 使用 AcceptHeaderLocaleResolver 从请求头 Accept-Language 中解析语言
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(DEFAULT_LOCALE);
        localeResolver.setSupportedLocales(SUPPORTED_LOCALES);
        return localeResolver;
    }

    /**
     * 配置 Validator 使用 MessageSource
     * 使验证注解支持国际化
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
