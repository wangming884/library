package com.library.config;

import com.library.security.LoginUser;
import com.library.service.SysLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "operationLogStartTime";
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final SysLogService logService;

    public OperationLogInterceptor(SysLogService logService) {
        this.logService = logService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (shouldLog(request)) {
            request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!shouldLog(request)) {
            return;
        }
        Long start = (Long) request.getAttribute(START_TIME_ATTR);
        long duration = start == null ? 0L : System.currentTimeMillis() - start;
        LoginUser user = currentUser();
        if (user == null || "reader".equals(user.getRoleKey())) {
            return;
        }

        String operation = request.getMethod() + " " + request.getRequestURI();
        int status = ex == null && response.getStatus() < 400 ? 1 : 0;
        try {
            logService.log(
                    user.getUserId(),
                    user.getUsername(),
                    operation,
                    request.getMethod(),
                    buildParams(request),
                    clientIp(request),
                    duration,
                    status);
        } catch (Exception ignored) {
            // Logging must never break the actual business request.
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null
                && uri.startsWith("/api/admin/")
                && !uri.startsWith("/api/admin/logs")
                && WRITE_METHODS.contains(request.getMethod());
    }

    private LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        return principal instanceof LoginUser ? (LoginUser) principal : null;
    }

    private String buildParams(HttpServletRequest request) {
        String query = request.getQueryString();
        if (!StringUtils.hasText(query)) {
            return "";
        }
        return query.length() > 1000 ? query.substring(0, 1000) : query;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }
}
