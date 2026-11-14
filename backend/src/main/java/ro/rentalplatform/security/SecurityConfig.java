package ro.rentalplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  JwtAuthFilter jwtAuthFilter,
                                                  RestAuthenticationEntryPoint authEntryPoint,
                                                  RestAccessDeniedHandler accessDeniedHandler,
                                                  CorsConfig corsConfig) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // „Anunțurile mele” rămân doar pentru proprietari; regula trebuie să stea
                        // înaintea celei publice, altfel /api/listings/{id} ar prinde și /mine.
                        .requestMatchers(HttpMethod.GET, "/api/listings/mine", "/api/listings/mine/**")
                                .hasRole("LANDLORD")
                        // Căutarea, harta și detaliile anunțurilor sunt publice (CAUT-01, DET-01).
                        .requestMatchers(HttpMethod.GET, "/api/listings", "/api/listings/map", "/api/listings/{id}")
                                .permitAll()
                        // „Cererile mele” rămân doar pentru chiriași; regula stă înaintea celei publice.
                        .requestMatchers(HttpMethod.GET, "/api/requests/mine", "/api/requests/mine/**")
                                .hasRole("TENANT")
                        // Căutarea și detaliile cererilor sunt publice; telefonul chiriașului doar pentru autentificați.
                        .requestMatchers(HttpMethod.GET, "/api/requests", "/api/requests/{id}")
                                .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
