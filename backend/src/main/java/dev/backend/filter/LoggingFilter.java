package dev.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        long startTime = System.currentTimeMillis();

        try {
            logger.info("Inbound request: {} {}", request.getMethod(), request.getRequestURI());
            response.addHeader("X-Request-ID", requestId);

            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Outbound response: {} ms", duration);
            MDC.remove("requestId");
        }
    }
}
