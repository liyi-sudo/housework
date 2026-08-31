package com.ecommerce.auth;

import com.ecommerce.common.BizException;
import com.ecommerce.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            throw new BizException(401, "管理员未登录或登录已过期");
        }
        try {
            if (!"ADMIN".equals(jwtUtil.parseClient(token))) {
                throw new BizException(401, "无管理员访问权限");
            }
            Long adminId = jwtUtil.parseUserId(token);
            UserContext.set(adminId);
            return true;
        } catch (BizException e) {
            UserContext.clear();
            throw e;
        } catch (Exception e) {
            UserContext.clear();
            throw new BizException(401, "登录已过期，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}