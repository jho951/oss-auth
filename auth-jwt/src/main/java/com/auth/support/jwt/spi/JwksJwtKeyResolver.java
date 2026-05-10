package com.auth.support.jwt.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.Decoders;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;

/** JWKS JSON 문서에서 검증 키를 읽어오는 resolver입니다. */
public final class JwksJwtKeyResolver implements JwtKeyResolver {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final Map<String, Key> keysById;
	private final Key singleKey;

	public JwksJwtKeyResolver(String jwksJson) {
		try {
			JsonNode root = OBJECT_MAPPER.readTree(jwksJson);
			JsonNode keys = root.get("keys");
			if (keys == null || !keys.isArray() || keys.isEmpty()) {
				throw new IllegalArgumentException("JWKS must contain keys");
			}
			LinkedHashMap<String, Key> parsed = new LinkedHashMap<>();
			Iterator<JsonNode> iterator = keys.elements();
			int index = 0;
			while (iterator.hasNext()) {
				JsonNode keyNode = iterator.next();
				Key key = toKey(keyNode);
				String keyId = keyNode.hasNonNull("kid") ? keyNode.get("kid").asText() : "key-" + index;
				parsed.put(keyId, key);
				index++;
			}
			this.keysById = Map.copyOf(parsed);
			this.singleKey = parsed.size() == 1 ? parsed.values().iterator().next() : null;
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid JWKS document", e);
		}
	}

	@Override
	public Optional<Key> resolve(String keyId) {
		if (keyId == null || keyId.isBlank()) {
			return Optional.ofNullable(singleKey);
		}
		return Optional.ofNullable(keysById.get(keyId));
	}

	private static Key toKey(JsonNode keyNode) throws Exception {
		String kty = text(keyNode, "kty");
		return switch (kty) {
			case "RSA" -> rsaKey(keyNode);
			case "EC" -> ecKey(keyNode);
			case "oct" -> octKey(keyNode);
			default -> throw new IllegalArgumentException("unsupported jwk kty: " + kty);
		};
	}

	private static Key rsaKey(JsonNode keyNode) throws Exception {
		BigInteger modulus = new BigInteger(1, Decoders.BASE64URL.decode(text(keyNode, "n")));
		BigInteger exponent = new BigInteger(1, Decoders.BASE64URL.decode(text(keyNode, "e")));
		return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
	}

	private static Key ecKey(JsonNode keyNode) throws Exception {
		AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		parameters.init(new ECGenParameterSpec(text(keyNode, "crv")));
		ECParameterSpec ecSpec = parameters.getParameterSpec(ECParameterSpec.class);
		BigInteger x = new BigInteger(1, Decoders.BASE64URL.decode(text(keyNode, "x")));
		BigInteger y = new BigInteger(1, Decoders.BASE64URL.decode(text(keyNode, "y")));
		return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), ecSpec));
	}

	private static Key octKey(JsonNode keyNode) {
		byte[] secret = Decoders.BASE64URL.decode(text(keyNode, "k"));
		return new SecretKeySpec(secret, "HmacSHA256");
	}

	private static String text(JsonNode node, String fieldName) {
		JsonNode field = node.get(fieldName);
		if (field == null || field.isNull() || field.asText().isBlank()) {
			throw new IllegalArgumentException("missing jwk field: " + fieldName);
		}
		return field.asText();
	}
}
