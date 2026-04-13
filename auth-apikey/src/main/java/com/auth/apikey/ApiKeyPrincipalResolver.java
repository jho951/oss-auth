package com.auth.apikey;

import com.auth.api.model.Principal;
import java.util.Optional;

/** Resolves valid API keys to principals. */
public interface ApiKeyPrincipalResolver {

	Optional<Principal> resolve(ApiKeyCredential credential);
}
