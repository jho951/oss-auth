package com.auth.hybrid.strategy;

import com.auth.api.authentication.AuthenticationSource;
import java.util.List;

/** Static source priority strategy. */
public final class SourceFirstHybridResolutionStrategy implements HybridResolutionStrategy {

	private final List<AuthenticationSource> sourceOrder;

	public SourceFirstHybridResolutionStrategy(List<AuthenticationSource> sourceOrder) {
		this.sourceOrder = sourceOrder == null || sourceOrder.isEmpty()
			? List.of(AuthenticationSource.JWT, AuthenticationSource.SESSION)
			: List.copyOf(sourceOrder);
	}

	public static SourceFirstHybridResolutionStrategy jwtThenSession() {
		return new SourceFirstHybridResolutionStrategy(List.of(AuthenticationSource.JWT, AuthenticationSource.SESSION));
	}

	public static SourceFirstHybridResolutionStrategy sessionThenJwt() {
		return new SourceFirstHybridResolutionStrategy(List.of(AuthenticationSource.SESSION, AuthenticationSource.JWT));
	}

	@Override
	public List<AuthenticationSource> sourceOrder() {
		return sourceOrder;
	}
}
