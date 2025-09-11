package com.easyshop.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.easyshop.auth.service.DatabaseRegisteredClientRepository;
import com.easyshop.auth.service.DatabaseUserDetailsService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.http.MediaType;


/**
 * Spring Authorization Server configuration with OIDC Discovery support.
 * Provides OAuth2 and OpenID Connect endpoints for authentication.
 */
@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {

    /**
     * Security filter chain for Authorization Server endpoints.
     * Configures OAuth2 and OIDC endpoints with proper security.
     * This is the recommended approach from Spring Authorization Server documentation.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())    // Enable OpenID Connect 1.0
                )
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }


    /**
     * Security filter chain for general application endpoints.
     * Allows access to login page and static resources.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login", "/error", "/webjars/**",
                                "/healthz", "/readyz", "/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * User details service for authentication backed by the database.
     */
    @Bean
    public UserDetailsService userDetailsService(DatabaseUserDetailsService userDetailsService) {
        return userDetailsService;
    }

    /**
     * Password encoder for user authentication.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Registered client repository with configured OAuth2 clients.
     * Includes webapp (PKCE), gateway (confidential), and service clients.
     */


    @Bean
    public RegisteredClientRepository registeredClientRepository(DatabaseRegisteredClientRepository databaseRepository) {
        return databaseRepository;
    }

    /**
     * Authorization server settings.
     * Configures issuer URL and other server settings.
     * Settings are loaded from application.yml
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .build();
    }
}
