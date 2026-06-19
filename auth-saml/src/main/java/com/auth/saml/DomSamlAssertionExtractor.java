package com.auth.saml;

import com.auth.core.api.exception.AuthException;
import com.auth.core.api.exception.AuthFailureReason;
import com.auth.core.utils.Strings;
import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/** DOM 파서를 이용해 SAML assertion의 핵심 필드를 추출하는 기본 구현입니다. */
public final class DomSamlAssertionExtractor implements SamlAssertionExtractor {

	@Override
	public SamlAssertion extract(String responseXml) {
		try {
			Document document = parse(responseXml);
			Element assertion = assertionElement(document);
			Element conditions = firstElement(assertion, "Conditions");
			Element subjectConfirmationData = firstElement(assertion, "SubjectConfirmationData");
			Element authnStatement = firstElement(assertion, "AuthnStatement");

			return new SamlAssertion(
				text(firstElement(assertion, "NameID")),
				text(firstElement(assertion, "Issuer")),
				texts(assertion, "Audience"),
				attribute(subjectConfirmationData, "Recipient"),
				attribute(subjectConfirmationData, "InResponseTo"),
				attribute(authnStatement, "SessionIndex"),
				instant(attribute(assertion, "IssueInstant")),
				instant(attribute(conditions, "NotBefore")),
				instant(attribute(conditions, "NotOnOrAfter")),
				attributes(assertion)
			);
		} catch (AuthException e) {
			throw e;
		} catch (Exception e) {
			throw new AuthException(AuthFailureReason.INVALID_TOKEN, "invalid SAML response XML", e);
		}
	}

	private static Document parse(String responseXml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		return factory.newDocumentBuilder().parse(new InputSource(new StringReader(responseXml)));
	}

	private static Element assertionElement(Document document) {
		NodeList assertions = document.getElementsByTagNameNS("*", "Assertion");
		if (assertions.getLength() > 0) {
			return (Element) assertions.item(0);
		}
		if (document.getDocumentElement() != null && "Assertion".equals(document.getDocumentElement().getLocalName())) {
			return document.getDocumentElement();
		}
		throw new AuthException(AuthFailureReason.INVALID_TOKEN, "missing SAML assertion");
	}

	private static Element firstElement(Element root, String localName) {
		if (root == null) return null;
		NodeList nodes = root.getElementsByTagNameNS("*", localName);
		return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
	}

	private static List<String> texts(Element root, String localName) {
		if (root == null) return com.auth.core.utils.CollectionUtils.listOf();
		NodeList nodes = root.getElementsByTagNameNS("*", localName);
		ArrayList<String> values = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			String value = text((Element) nodes.item(i));
			if (!Strings.isBlank(value)) {
				values.add(value);
			}
		}
		return com.auth.core.utils.CollectionUtils.copyList(values);
	}

	private static Map<String, Object> attributes(Element assertion) {
		NodeList nodes = assertion.getElementsByTagNameNS("*", "Attribute");
		LinkedHashMap<String, Object> values = new LinkedHashMap<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Element attribute = (Element) nodes.item(i);
			String name = attribute(attribute, "Name");
			if (Strings.isBlank(name)) continue;
			List<String> entries = texts(attribute, "AttributeValue");
			if (entries.isEmpty()) continue;
			values.put(name, entries.size() == 1 ? entries.get(0) : entries);
		}
		return com.auth.core.utils.CollectionUtils.copyMap(values);
	}

	private static String text(Element element) {
		return element == null || element.getTextContent() == null ? "" : element.getTextContent().trim();
	}

	private static String attribute(Element element, String attributeName) {
		if (element == null) return "";
		String value = element.getAttribute(attributeName);
		return value == null ? "" : value.trim();
	}

	private static Instant instant(String value) {
		return Strings.isBlank(value) ? null : Instant.parse(value);
	}
}
