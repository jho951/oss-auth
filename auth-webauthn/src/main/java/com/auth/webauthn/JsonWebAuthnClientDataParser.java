package com.auth.webauthn;

import com.auth.core.utils.Strings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** JSON 형식의 `clientDataJSON` 문자열을 파싱하는 기본 구현입니다. */
public final class JsonWebAuthnClientDataParser implements WebAuthnClientDataParser {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public WebAuthnClientData parse(String clientDataJson) {
		if (Strings.isBlank(clientDataJson)) throw new IllegalArgumentException("clientDataJson must not be blank");
		try {
			JsonNode root = OBJECT_MAPPER.readTree(clientDataJson);
			return new WebAuthnClientData(
				text(root, "type"),
				text(root, "challenge"),
				text(root, "origin"),
				root.path("crossOrigin").asBoolean(false)
			);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid clientDataJson", e);
		}
	}

	private static String text(JsonNode node, String fieldName) {
		JsonNode field = node.get(fieldName);
		if (field == null || field.isNull() || Strings.isBlank(field.asText())) {
			throw new IllegalArgumentException("missing clientData field: " + fieldName);
		}
		return field.asText();
	}
}
