package com.note.noteserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 注册拦截器等
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LocaleInterceptor localeInterceptor;
    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册语言环境拦截器，应用到所有路径
        registry.addInterceptor(localeInterceptor)
                .addPathPatterns("/**");

        // 注册鉴权拦截器：除公开接口外，其他接口都需要登录
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/health",
                        "/auth/login",
                        "/auth/register",
                        "/auth/refresh"
                );
    }
}
