package com.auth.apikey;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth.api.model.Principal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ApiKeyAuthenticationProviderTest {

	@Test
	void authenticatesResolvedApiKey() {
		Principal principal = new Principal("api-client");
		ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(credential ->
			"key-1".equals(credential.keyId()) && "secret".equals(credential.secret())
				? Optional.of(principal)
				: Optional.empty()
		);

		assertThat(provider.authenticate(new ApiKeyCredential("key-1", "secret"))).contains(principal);
		assertThat(provider.authenticate(new ApiKeyCredential("key-1", "wrong"))).isEmpty();
	}
}
