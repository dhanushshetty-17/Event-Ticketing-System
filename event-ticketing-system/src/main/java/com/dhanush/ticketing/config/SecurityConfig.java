
package com.dhanush.ticketing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret must be at least 32 bytes for HS256"
            );
        }

        SecretKeySpec secretKey =
                new SecretKeySpec(secretBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            String role = jwt.getClaimAsString("role");

            var authorities = role == null
                    ? Collections.<SimpleGrantedAuthority>emptyList()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));

            return new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    jwt.getSubject()
            );
        };
    }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiRateLimitFilter apiRateLimitFilter) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/organizer/events")
                        .hasRole("ORGANIZER")
                        .requestMatchers(HttpMethod.GET, "/api/tickets/verify/**")
                        .hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/venues/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ORGANIZER")
                        .requestMatchers(
                                HttpMethod.POST, "/api/events/**"
                        ).hasRole("ORGANIZER")

                        .requestMatchers(
                                HttpMethod.PUT, "/api/events/**"
                        ).hasRole("ORGANIZER")

                        .requestMatchers(
                                HttpMethod.PATCH, "/api/events/**"
                        ).hasRole("ORGANIZER")

                        .requestMatchers(
                                HttpMethod.DELETE, "/api/events/**"
                        ).hasRole("ORGANIZER")
                        .requestMatchers(
                                HttpMethod.POST, "/api/venues/**"
                        ).hasRole("ORGANIZER")
                        .requestMatchers(
                                HttpMethod.PUT, "/api/venues/**"
                        ).hasRole("ORGANIZER")
                        .requestMatchers(
                                HttpMethod.PATCH, "/api/venues/**"
                        ).hasRole("ORGANIZER")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/venues/**"
                        ).hasRole("ORGANIZER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        http.addFilterAfter(apiRateLimitFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public ApiRateLimitFilter apiRateLimitFilter(
            org.redisson.api.RedissonClient redissonClient) {
        return new ApiRateLimitFilter(redissonClient);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));

        configuration.setAllowCredentials(false);
        if (configuration.getAllowCredentials()
                && origins.contains("*")) {
            throw new IllegalStateException(
                    "Wildcard CORS origins cannot be used with credentials");
        }
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
