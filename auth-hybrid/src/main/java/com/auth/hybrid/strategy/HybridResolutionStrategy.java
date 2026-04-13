package com.auth.hybrid.strategy;

import com.auth.api.authentication.AuthenticationSource;
import java.util.List;

/** Determines source order for hybrid authentication without service-specific policy. */
public interface HybridResolutionStrategy {

	List<AuthenticationSource> sourceOrder();
}
