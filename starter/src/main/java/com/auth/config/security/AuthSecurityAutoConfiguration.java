package com.auth.config.security;

import com.auth.config.AuthProperties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;

@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix="auth", name="auto-security", havingValue="true", matchIfMissing=true)
public class AuthSecurityAutoConfiguration {

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	@ConditionalOnMissingBean(SecurityFilterChain.class)
	public SecurityFilterChain authDefaultSecurityFilterChain(
		HttpSecurity http,
		AuthOncePerRequestFilter authFilter,
		AuthProperties props,
		@Qualifier("authOAuth2AuthenticationSuccessHandler")
		ObjectProvider<AuthenticationSuccessHandler> oauth2SuccessHandlerProvider,
		@Qualifier("authOAuth2AuthenticationFailureHandler")
		ObjectProvider<AuthenticationFailureHandler> oauth2FailureHandlerProvider
	) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.httpBasic(basic -> basic.disable())
			.formLogin(form -> form.disable())
			.logout(logout -> logout.disable())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((req, res, e) -> {
					res.setStatus(401);
					res.setCharacterEncoding(StandardCharsets.UTF_8.name());
					res.setContentType(MediaType.APPLICATION_JSON_VALUE);
					res.getWriter().write("{\"message\":\"UNAUTHORIZED\"}");
				})
				.accessDeniedHandler((req, res, e) -> {
					res.setStatus(403);
					res.setCharacterEncoding(StandardCharsets.UTF_8.name());
					res.setContentType(MediaType.APPLICATION_JSON_VALUE);
					res.getWriter().write("{\"message\":\"FORBIDDEN\"}");
				})
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers(
					props.getOauth2().getAuthorizationBaseUri() + "/**",
					props.getOauth2().getLoginProcessingBaseUri()
				).permitAll()
				.anyRequest().authenticated()
			);

		AuthenticationSuccessHandler oauth2SuccessHandler = oauth2SuccessHandlerProvider.getIfAvailable();
		if (props.getOauth2().isEnabled() && oauth2SuccessHandler != null) {
			http.oauth2Login(oauth2 -> {
				oauth2.authorizationEndpoint(endpoint ->
					endpoint.baseUri(props.getOauth2().getAuthorizationBaseUri())
				);
				oauth2.redirectionEndpoint(endpoint ->
					endpoint.baseUri(props.getOauth2().getLoginProcessingBaseUri())
				);
				oauth2.successHandler(oauth2SuccessHandler);
				AuthenticationFailureHandler oauth2FailureHandler = oauth2FailureHandlerProvider.getIfAvailable();
				if (oauth2FailureHandler != null) {
					oauth2.failureHandler(oauth2FailureHandler);
				}
			});
		}

		http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
