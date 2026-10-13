package ai.anvaya.prajna.config;

import ai.anvaya.prajna.security.KeycloakJwtAuthenticationConverter;
import ai.anvaya.prajna.security.KeycloakSecurityContextFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeycloakSecurityContextFilter securityContextFilter;
    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;
    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;

    public SecurityConfig(KeycloakSecurityContextFilter securityContextFilter,
                          KeycloakJwtAuthenticationConverter jwtAuthenticationConverter,
                          ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        this.securityContextFilter = securityContextFilter;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .requestMatchers("/api/v1/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        if (jwtDecoderProvider.getIfAvailable() != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            );
        }

        http.addFilterBefore(securityContextFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
