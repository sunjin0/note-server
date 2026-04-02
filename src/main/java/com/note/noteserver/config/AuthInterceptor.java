package com.note.noteserver.config;

import com.note.noteserver.exception.UnauthorizedException;
import com.note.noteserver.util.I18nMessageUtil;
import com.note.noteserver.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权/权限拦截器
 * 统一校验 Authorization: Bearer <access token>
 * 并将 userId/username 写入 request attribute，供 Controller 使用。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_USERNAME = "username";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 非 Controller 方法直接放行（例如静态资源等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException(I18nMessageUtil.getMessage("error.auth.invalid.auth.header"));
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtUtil.parseClaims(token);

            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                throw new UnauthorizedException(I18nMessageUtil.getMessage("error.auth.invalid.token.type"));
            }

            request.setAttribute(ATTR_USER_ID, claims.get("userId", String.class));
            request.setAttribute(ATTR_USERNAME, claims.get("username", String.class));
            return true;
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(I18nMessageUtil.getMessage("error.auth.token.expired"), e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(I18nMessageUtil.getMessage("error.auth.invalid.token"), e);
        }
    }
}
