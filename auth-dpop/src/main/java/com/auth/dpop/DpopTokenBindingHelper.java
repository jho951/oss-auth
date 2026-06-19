package com.auth.dpop;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import com.auth.core.utils.Strings;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;

/** DPoP access-token 해시, URI 정규화, 내장 JWK 변환을 위한 도우미입니다. */
public final class DpopTokenBindingHelper {

	private DpopTokenBindingHelper() {
	}

	public static String accessTokenHash(String accessToken) {
		if (Strings.isBlank(accessToken)) throw new IllegalArgumentException("accessToken must not be blank");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Encoders.BASE64URL.encode(digest.digest(accessToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("failed to hash access token", e);
		}
	}

	public static String normalizeUri(String value) {
		if (Strings.isBlank(value)) throw new IllegalArgumentException("uri must not be blank");
		try {
			URI uri = new URI(value).normalize();
			return new URI(
				uri.getScheme() == null ? null : uri.getScheme().toLowerCase(),
				uri.getUserInfo(),
				uri.getHost() == null ? null : uri.getHost().toLowerCase(),
				uri.getPort(),
				uri.getPath(),
				uri.getQuery(),
				null
			).toString();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("invalid uri", e);
		}
	}

	public static PublicKey toPublicKey(Map<String, Object> jwk) {
		if (jwk == null || jwk.isEmpty()) throw new IllegalArgumentException("jwk must not be empty");
		String kty = stringValue(jwk.get("kty"));
		try {
			if ("RSA".equals(kty)) {
				return rsaPublicKey(jwk);
			}
			if ("EC".equals(kty)) {
				return ecPublicKey(jwk);
			}
			throw new IllegalArgumentException("unsupported jwk kty: " + kty);
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid jwk", e);
		}
	}

	public static String jwkThumbprint(Map<String, Object> jwk) {
		if (jwk == null || jwk.isEmpty()) throw new IllegalArgumentException("jwk must not be empty");
		String kty = stringValue(jwk.get("kty"));
		String canonical;
		if ("RSA".equals(kty)) {
			canonical = "{\"e\":\"" + stringValue(jwk.get("e")) + "\",\"kty\":\"RSA\",\"n\":\"" + stringValue(jwk.get("n")) + "\"}";
		} else if ("EC".equals(kty)) {
			canonical = "{\"crv\":\"" + stringValue(jwk.get("crv")) + "\",\"kty\":\"EC\",\"x\":\"" + stringValue(jwk.get("x")) + "\",\"y\":\"" + stringValue(jwk.get("y")) + "\"}";
		} else {
			throw new IllegalArgumentException("unsupported jwk kty: " + kty);
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Encoders.BASE64URL.encode(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("failed to compute JWK thumbprint", e);
		}
	}

	public static Map<String, Object> publicJwk(PublicKey key, String keyId) {
		if (key instanceof RSAPublicKey) {
			RSAPublicKey rsaPublicKey = (RSAPublicKey) key;
			return com.auth.core.utils.CollectionUtils.mapOf(
				"kty", "RSA",
				"kid", keyId == null ? "" : keyId,
				"n", Encoders.BASE64URL.encode(unsignedBytes(rsaPublicKey.getModulus())),
				"e", Encoders.BASE64URL.encode(unsignedBytes(rsaPublicKey.getPublicExponent()))
			);
		}
		if (key instanceof ECPublicKey) {
			ECPublicKey ecPublicKey = (ECPublicKey) key;
			String curve = curveName(ecPublicKey.getParams());
			return com.auth.core.utils.CollectionUtils.mapOf(
				"kty", "EC",
				"kid", keyId == null ? "" : keyId,
				"crv", curve,
				"x", Encoders.BASE64URL.encode(unsignedBytes(ecPublicKey.getW().getAffineX())),
				"y", Encoders.BASE64URL.encode(unsignedBytes(ecPublicKey.getW().getAffineY()))
			);
		}
		throw new IllegalArgumentException("unsupported public key type: " + key.getAlgorithm());
	}

	private static PublicKey rsaPublicKey(Map<String, Object> jwk) throws Exception {
		BigInteger modulus = new BigInteger(1, Decoders.BASE64URL.decode(stringValue(jwk.get("n"))));
		BigInteger exponent = new BigInteger(1, Decoders.BASE64URL.decode(stringValue(jwk.get("e"))));
		return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
	}

	private static PublicKey ecPublicKey(Map<String, Object> jwk) throws Exception {
		String curve = stringValue(jwk.get("crv"));
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(curve));
		ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
		BigInteger x = new BigInteger(1, Decoders.BASE64URL.decode(stringValue(jwk.get("x"))));
		BigInteger y = new BigInteger(1, Decoders.BASE64URL.decode(stringValue(jwk.get("y"))));
		return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), ecParameterSpec));
	}

	private static String stringValue(Object value) {
		if (value == null) throw new IllegalArgumentException("missing JWK field");
		return String.valueOf(value);
	}

	private static String curveName(ECParameterSpec spec) {
		int fieldSize = spec.getCurve().getField().getFieldSize();
		switch (fieldSize) {
			case 256:
				return "secp256r1";
			case 384:
				return "secp384r1";
			case 521:
				return "secp521r1";
			default:
				throw new IllegalArgumentException("unsupported EC field size: " + fieldSize);
		}
	}

	private static byte[] unsignedBytes(BigInteger value) {
		byte[] bytes = value.toByteArray();
		if (bytes.length > 1 && bytes[0] == 0) {
			byte[] trimmed = new byte[bytes.length - 1];
			System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
			return trimmed;
		}
		return bytes;
	}
}
