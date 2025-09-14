package net.alishahidi.mcpconductor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.beans.factory.annotation.Value;

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
    public CommandValidator commandValidator() {
        return new CommandValidator();
    }

    @Bean
    public PathValidator pathValidator() {
        return new PathValidator();
    }

    @Bean
    public RateLimiter rateLimiter() {
        return new RateLimiter();
    }

    @Bean
    public AuditLogger auditLogger() {
        return new AuditLogger();
    }
}