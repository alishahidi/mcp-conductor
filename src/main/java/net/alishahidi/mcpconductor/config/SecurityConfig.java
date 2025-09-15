package net.alishahidi.mcpconductor.config;

import net.alishahidi.mcpconductor.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(csrf -> csrf.disable());
        } else {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                            .requestMatchers("/mcp/**").authenticated()
                            .anyRequest().authenticated()
                    )
                    .httpBasic(Customizer.withDefaults())
                    .sessionManagement(session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .csrf(csrf -> csrf.disable());
        }
        return http.build();
    }

    @Bean
    public CommandValidator commandValidator(@Value("${security.command.strict-mode:true}") boolean strictMode,
                                             @Value("${security.command.allowed}") List<String> allowedCommands) {
        return new CommandValidator(strictMode, allowedCommands);
    }

    @Bean
    public PathValidator pathValidator(@Value("${security.path.allowed}") List<String> allowedPaths,
                                       @Value("${security.path.blocked}") List<String> blockedPaths) {
        return new PathValidator(allowedPaths, blockedPaths);
    }

    @Bean
    public RateLimiter rateLimiter(@Value("${rate-limit.capacity:100}") int capacity,
                                   @Value("${rate-limit.refill-tokens:100}") int refillTokens,
                                   @Value("${rate-limit.refill-duration-minutes:1}") int refillDurationMinutes) {
        return new RateLimiter(capacity, refillTokens, refillDurationMinutes);
    }

    @Bean
    public AuditLogger auditLogger(ObjectMapper objectMapper) {
        return new AuditLogger(objectMapper);
    }
}