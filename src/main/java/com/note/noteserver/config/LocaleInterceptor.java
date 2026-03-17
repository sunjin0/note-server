package com.note.noteserver.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * 语言环境拦截器
 * 支持通过 lang 请求参数切换语言
 * 例如: ?lang=en 或 ?lang=zh_CN
 */
@Component
public class LocaleInterceptor implements HandlerInterceptor {

    private static final String LANG_PARAM = "lang";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String lang = request.getParameter(LANG_PARAM);
        
        if (StringUtils.hasText(lang)) {
            Locale locale = parseLocale(lang);
            if (isSupportedLocale(locale)) {
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                if (localeResolver != null) {
                    localeResolver.setLocale(request, response, locale);
                    LocaleContextHolder.setLocale(locale);
                }
            }
        }
        
        return true;
    }

    /**
     * 解析语言字符串为 Locale 对象
     */
    private Locale parseLocale(String lang) {
        if (!StringUtils.hasText(lang)) {
            return MessageSourceConfig.DEFAULT_LOCALE;
        }
        
        // 处理下划线或连字符分隔的语言代码
        String normalized = lang.replace("-", "_");
        String[] parts = normalized.split("_");
        
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length >= 2) {
            return new Locale(parts[0], parts[1]);
        }
        
        return MessageSourceConfig.DEFAULT_LOCALE;
    }

    /**
     * 检查是否是支持的语言
     */
    private boolean isSupportedLocale(Locale locale) {
        return MessageSourceConfig.SUPPORTED_LOCALES.stream()
                .anyMatch(supported -> supported.getLanguage().equals(locale.getLanguage()));
    }
}
