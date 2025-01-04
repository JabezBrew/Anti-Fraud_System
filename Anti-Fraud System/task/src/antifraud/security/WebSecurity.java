package antifraud.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class WebSecurity {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public WebSecurity(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configure HTTP Basic + custom entry point
                .httpBasic(httpBasic ->
                        httpBasic.authenticationEntryPoint(restAuthenticationEntryPoint)
                )

                // 2. Disable CSRF & frameOptions (for Postman, H2 console, etc.)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // 3. (Optional) Force HTTPS: uncomment if desired
                // .requiresChannel(channel -> channel.anyRequest().requiresSecure())

                // 4. Authorize requests
                .authorizeHttpRequests(authz -> authz
                        // Allow requests for ASYNC/FORWARD/ERROR dispatch types
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // Permit user creation + actuator shutdown
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()

                        // ADMINISTRATOR, SUPPORT, etc. role-based restrictions
                        .requestMatchers("/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                        .requestMatchers("/api/auth/user/{username}", "/api/auth/access", "/api/auth/role").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT")
                        .requestMatchers(
                                "/api/antifraud/suspicious-ip", "/api/antifraud/suspicious-ip/{ip}",
                                "/api/antifraud/stolencard", "/api/antifraud/stolencard/{number}",
                                "/api/antifraud/history", "/api/antifraud/history/{number}"
                        ).hasRole("SUPPORT")
                )

                // 5. Session management (STATELESS)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 6. Exception handling
                .exceptionHandling(ex ->
                        ex.accessDeniedHandler(new CustomAccessDeniedHandler())
                );

        // Build and return the configured SecurityFilterChain
        return http.build();
    }
}