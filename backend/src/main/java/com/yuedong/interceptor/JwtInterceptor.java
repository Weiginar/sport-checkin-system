package com.yuedong.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuedong.common.Result;
import com.yuedong.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "未登录或令牌缺失");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            sendError(response, 401, "令牌无效或已过期");
            return false;
        }

        // 将用户信息放入request属性
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("role", jwtUtil.getRole(token));
        return true;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        Result<?> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
