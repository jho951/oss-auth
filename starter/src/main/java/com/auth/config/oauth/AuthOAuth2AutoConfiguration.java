package com.auth.config.oauth;

import com.auth.config.controller.RefreshCookieWriter;
import com.auth.core.service.AuthService;
import com.auth.spi.OAuth2PrincipalResolver;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter")
@ConditionalOnBean(OAuth2PrincipalResolver.class)
@ConditionalOnProperty(prefix = "auth.oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthOAuth2AutoConfiguration {

	@Bean("authOAuth2AuthenticationSuccessHandler")
	@ConditionalOnMissingBean(name = "authOAuth2AuthenticationSuccessHandler")
	public AuthenticationSuccessHandler authOAuth2AuthenticationSuccessHandler(
		OAuth2PrincipalResolver principalResolver,
		AuthService authService,
		RefreshCookieWriter refreshCookieWriter
	) {
		return new OAuth2AuthenticationSuccessHandler(principalResolver, authService, refreshCookieWriter);
	}

	@Bean("authOAuth2AuthenticationFailureHandler")
	@ConditionalOnMissingBean(name = "authOAuth2AuthenticationFailureHandler")
	public AuthenticationFailureHandler authOAuth2AuthenticationFailureHandler() {
		return new OAuth2AuthenticationFailureHandler();
	}
}
