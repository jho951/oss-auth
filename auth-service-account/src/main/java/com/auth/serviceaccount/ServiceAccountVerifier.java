package com.auth.serviceaccount;

import com.auth.api.model.Principal;
import java.util.Optional;

/** Verifies service account credentials and returns a service principal. */
public interface ServiceAccountVerifier {

	Optional<Principal> verify(ServiceAccountCredential credential);
}
