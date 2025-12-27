package com.quokka.jobmate_connect.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quokka.jobmate_connect.dto.ApiResponse;
import com.quokka.jobmate_connect.entity.User;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Chạy sau OAuth2ResourceServerFilter (order 0)
public class UserStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/",
            "/users/registration",
            "/v3/api-docs/",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/internal/",
            "/jobs/available"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Bỏ qua các endpoint public
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Chỉ kiểm tra nếu đã được authenticate (có JWT token hợp lệ)
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            try {
                // Lấy userId từ JWT claims
                String userIdClaim = jwt.getClaimAsString("userId");
                if (userIdClaim != null) {
                    UUID userId = UUID.fromString(userIdClaim);

                    // Kiểm tra user status từ database
                    User user = userRepository.findById(userId)
                            .orElse(null);

                    if (user != null && "BANNED".equalsIgnoreCase(user.getStatus())) {
                        log.warn("Banned user attempted to access: {} - UserId: {}", requestPath, userId);

                        // Trả về response lỗi
                        response.setStatus(ErrorCode.USER_BANNED.getStatusCode().value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                        ApiResponse<?> apiResponse = ApiResponse.error(
                                ErrorCode.USER_BANNED,
                                ErrorCode.USER_BANNED.getMessage());

                        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        response.flushBuffer();
                        return;
                    }
                }
            } catch (Exception e) {
                // Log lỗi nhưng không block request nếu có lỗi khi kiểm tra
                log.error("Error checking user status in filter", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestPath) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (requestPath.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }
}
