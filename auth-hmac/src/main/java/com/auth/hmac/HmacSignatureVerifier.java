package com.auth.hmac;

/** Verifies a canonical HMAC request signature. */
public interface HmacSignatureVerifier {

	boolean verify(HmacAuthenticationRequest request, byte[] secret);
}
