package com.cognizant.retailpos.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "STORE_MANAGER")
                .requestMatchers("/api/reports/**")
                    .hasAnyRole("ADMIN", "STORE_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/promotions/**").authenticated()
                .requestMatchers("/api/promotions/**")
                    .hasAnyRole("ADMIN", "STORE_MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/inventory/**").authenticated()
                .requestMatchers("/api/products/**")
                    .hasAnyRole("ADMIN", "STORE_MANAGER")
                .requestMatchers("/api/inventory/**")
                    .hasAnyRole("ADMIN", "STORE_MANAGER", "INVENTORY_ASSOCIATE")
                .requestMatchers("/api/transactions/**")
                    .hasAnyRole("ADMIN", "STORE_MANAGER", "CASHIER")
                .requestMatchers("/api/products/**", "/api/dashboard/**", "/api/auth/**")
                    .authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) ->
                    writeJson(response, HttpServletResponse.SC_OK,
                            "{\"message\":\"Login successful\",\"username\":\""
                                    + authentication.getName() + "\"}"))
                .failureHandler((request, response, exception) ->
                    writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "{\"message\":\"Invalid username or password\"}"))
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) ->
                    writeJson(response, HttpServletResponse.SC_OK, "{\"message\":\"Logout successful\"}"))
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true))
            .exceptionHandling(errors -> errors
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> request.getRequestURI().startsWith("/api/")))
            .sessionManagement(session -> session.maximumSessions(1));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static void writeJson(HttpServletResponse response, int status, String body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(body);
    }
}
