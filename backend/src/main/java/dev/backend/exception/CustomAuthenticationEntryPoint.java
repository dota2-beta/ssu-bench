package dev.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        String requestId = MDC.get("requestId");

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        String jsonResponse = String.format(
                "{\"request_id\": \"%s\", \"error\": \"Unauthorized\", \"message\": \"Full authentication is required to access this resource\"}",
                requestId != null ? requestId : "n/a"
        );

        response.getWriter().write(jsonResponse);
    }
}
