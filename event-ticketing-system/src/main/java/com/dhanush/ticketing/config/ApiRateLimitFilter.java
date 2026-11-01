package com.dhanush.ticketing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiRateLimitFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;

    public ApiRateLimitFilter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Limit limit = limitFor(request);
        if (limit == null || isAllowed(limit, request)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(limit.retryAfterSeconds()));
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"rate_limit_exceeded\"}");
    }

    private boolean isAllowed(Limit limit, HttpServletRequest request) {
        String identity = limit.perUser()
                ? currentUserOrIp(request)
                : request.getRemoteAddr();
        RRateLimiter limiter = redissonClient.getRateLimiter(
                "api-rate-limit:" + limit.name() + ":" + identity);
        limiter.trySetRate(
                RateType.OVERALL,
                limit.permits(),
                limit.interval(),
                limit.unit());
        return limiter.tryAcquire();
    }

    private String currentUserOrIp(HttpServletRequest request) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                ? authentication.getName()
                : request.getRemoteAddr();
    }

    private Limit limitFor(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        return switch (request.getRequestURI()) {
            case "/api/auth/login" ->
                    new Limit("login", 5, 60, RateIntervalUnit.SECONDS, false, 60);
            case "/api/users" ->
                    new Limit("registration", 3, 1, RateIntervalUnit.HOURS, false, 3600);
            case "/api/reservations" ->
                    new Limit("reservation", 10, 60, RateIntervalUnit.SECONDS, true, 60);
            default -> null;
        };
    }

    private record Limit(
            String name,
            long permits,
            long interval,
            RateIntervalUnit unit,
            boolean perUser,
            long retryAfterSeconds) {
    }
}
