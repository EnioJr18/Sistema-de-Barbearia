package com.seuapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Desliga uma proteção de formulários HTML
                .csrf(csrf -> csrf.disable())

                // 2. Avisa que não vamos guardar "sessão" na memória (o Porteiro vai exigir o crachá JWT em cada pedido)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. AS REGRAS DA PORTA!
                .authorizeHttpRequests(authorize -> authorize
                        // Libera o cadastro de novos usuários (qualquer um pode criar uma conta)
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()

                        // Libera uma futura rota de login (que vamos criar daqui a pouco)
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()

                        // Exige o crachá JWT para TODO O RESTO (listar serviços, agendar, deletar, etc)
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}