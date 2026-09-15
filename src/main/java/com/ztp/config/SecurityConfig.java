package com.ztp.config;

import com.ztp.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
	
    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:5176}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        java.util.Set<String> origins = new java.util.LinkedHashSet<>();
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            origins.add(frontendUrl.trim());
        }
        origins.add("https://ztp-frontend.vercel.app");
        origins.add("http://localhost:5176");
        origins.add("http://127.0.0.1:5176");
        origins.add("http://localhost:5173");
        origins.add("http://127.0.0.1:5173");
        origins.add("http://localhost:3000");
        origins.add("http://127.0.0.1:3000");

        config.setAllowedOrigins(new java.util.ArrayList<>(origins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("XSRF-TOKEN", "Set-Cookie"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @org.springframework.beans.factory.annotation.Value("${app.cookie.same-site:None}")
    private String cookieSameSite;

    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Bean
    public org.springframework.session.web.http.CookieSerializer cookieSerializer() {
        var serializer = new org.springframework.session.web.http.DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setSameSite(cookieSameSite);
        serializer.setUseSecureCookie(cookieSecure);
        serializer.setUseHttpOnlyCookie(true);
        serializer.setCookiePath("/");
        return serializer;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(builder -> builder
            .sameSite(cookieSameSite)
            .secure(cookieSecure)
            .path("/"));
        return repository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
	
	@Bean
	public org.springframework.session.security.SpringSessionBackedSessionRegistry<?> sessionRegistry(
			org.springframework.session.FindByIndexNameSessionRepository<? extends org.springframework.session.Session> sessionRepository) {
		return new org.springframework.session.security.SpringSessionBackedSessionRegistry<>(sessionRepository);
	}
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CsrfTokenRepository csrfTokenRepository,
													 CorsConfigurationSource corsConfigurationSource,
													 org.springframework.security.core.session.SessionRegistry sessionRegistry) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.csrf(csrf -> csrf
				.csrfTokenRepository(csrfTokenRepository)
				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
			)
			.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.sessionManagement(session -> session
				.maximumSessions(5)          // a real "session management" limit from your brief
				.sessionRegistry(sessionRegistry)
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(restAuthenticationEntryPoint)
				.accessDeniedHandler(restAccessDeniedHandler)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/api/auth/me").authenticated()
				.requestMatchers("/api/auth/**", "/api/csrf-token").permitAll()
				.requestMatchers(HttpMethod.GET, "/uploads/avatars/**").permitAll()
				.anyRequest().authenticated()
			);

		return http.build();
	}


}