package com.quokka.jobmate_connect.configuration;



import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomJwtDecoder customJwtDecoder;

        private final String[] PUBLIC_ENDPOINTS = {
                        "/auth/**", // login, outbound, introspect, refresh, logout, verify-otp
                        "/users/registration",
                        "/notification/email/send"
        };

        private final String[] PUBLIC_SWAGGER = {
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests((requests) -> requests
                                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.GET, PUBLIC_SWAGGER).permitAll()
                                                .anyRequest().authenticated());

                http.oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwtConfigurer -> jwtConfigurer
                                                .decoder(customJwtDecoder)
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

                http.csrf(AbstractHttpConfigurer::disable);
                return http.build();
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return converter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public RestTemplate restTemplate() {
                // Dùng factory đơn giản có sẵn trong JDK, không cần httpclient5
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(10_000);
                factory.setReadTimeout(10_000);

                RestTemplate restTemplate = new RestTemplate(factory);
                restTemplate.getInterceptors().add((request, body, execution) -> {
                        request.getHeaders().add("User-Agent", "JobMateConnect/1.0 (contact@jobmate.com)");
                        request.getHeaders().add("Accept-Language", "en");
                        return execution.execute(request, body);
                });
                return restTemplate;
        }
}
