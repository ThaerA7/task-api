package com.example.tasks.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

@Configuration
@EnableMethodSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwt) throws Exception {
  return http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) -> res.sendError(401))  // unauthenticated -> 401
        .accessDeniedHandler((req, res, ex) -> res.sendError(403))       // forbidden -> 403
      )
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/ping", "/api/auth/**",
                           "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
          .anyRequest().authenticated()
      )
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
      .build();
}

}