package com.auth.oidc;

import com.auth.api.model.Principal;

/** Maps verified OIDC identity to the auth principal model. */
public interface OidcPrincipalMapper {

	Principal map(OidcIdentity identity);
}
