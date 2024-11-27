package antifraud.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class WebSecurity {

    RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public WebSecurity(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)//handle authentication errors
                .and()
                .csrf().disable().headers().frameOptions().disable()// for Postman, the H2 console
                .and()
//                .requiresChannel().anyRequest().requiresSecure()// force HTTPS
//                .and()
                .authorizeHttpRequests()
                .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .requestMatchers("/actuator/shutdown").permitAll()
                .requestMatchers("/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                .requestMatchers("/api/auth/user/{username}", "/api/auth/access", "/api/auth/role").hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT")
                .requestMatchers("/api/antifraud/suspicious-ip", "/api/antifraud/suspicious-ip/{ip}",
                        "/api/antifraud/stolencard", "/api/antifraud/stolencard/{number}",
                        "/api/antifraud/history", "/api/antifraud/history/{number}").hasRole("SUPPORT")
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                .and()
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler()); // Handle authorization errors
        return http.build();
    }

}
