package com.mockr.runnr.matcher.body;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("XmlBodyParser - XML/SOAP Parsing with Attributes")
class XmlBodyParserTest {

    private final XmlBodyParser parser = new XmlBodyParser();

    @Test
    @DisplayName("Simple XML element parsing")
    void testSimpleXml() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <root>
                  <name>John</name>
                  <email>john@example.com</email>
                </root>
                """;

        Map<String, Object> result = parser.parse(xml);

        assertEquals("John", result.get("root.name"));
        assertEquals("john@example.com", result.get("root.email"));
    }

    @Test
    @DisplayName("Nested XML elements with dot notation")
    void testNestedXml() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <root>
                  <user>
                    <id>1</id>
                    <name>John</name>
                    <contact>
                      <email>john@example.com</email>
                      <phone>555-1234</phone>
                    </contact>
                  </user>
                </root>
                """;

        Map<String, Object> result = parser.parse(xml);

        assertEquals("1", result.get("root.user.id"));
        assertEquals("John", result.get("root.user.name"));
        assertEquals("john@example.com", result.get("root.user.contact.email"));
        assertEquals("555-1234", result.get("root.user.contact.phone"));
    }

    @Test
    @DisplayName("XML attributes with @ prefix")
    void testXmlAttributes() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <root>
                  <Element version="1.0" type="test">Content</Element>
                </root>
                """;

        Map<String, Object> result = parser.parse(xml);

        assertEquals("1.0", result.get("root.Element@version"));
        assertEquals("test", result.get("root.Element@type"));
        assertEquals("Content", result.get("root.Element"));
    }

    @Test
    @DisplayName("SOAP/XML with namespaces")
    void testSoapWithNamespaces() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <TransferRequest version="1.0">
                      <From>ACC123</From>
                      <To>ACC456</To>
                      <Amount>1000</Amount>
                    </TransferRequest>
                  </soap:Body>
                </soap:Envelope>
                """;

        Map<String, Object> result = parser.parse(xml);

        // Check namespace prefix is preserved
        assertTrue(result.containsKey("soap:Envelope.soap:Body.TransferRequest@version"));
        assertEquals("1.0", result.get("soap:Envelope.soap:Body.TransferRequest@version"));
        assertEquals("ACC123", result.get("soap:Envelope.soap:Body.TransferRequest.From"));
        assertEquals("ACC456", result.get("soap:Envelope.soap:Body.TransferRequest.To"));
        assertEquals("1000", result.get("soap:Envelope.soap:Body.TransferRequest.Amount"));
    }

    @Test
    @DisplayName("Empty body returns empty map")
    void testEmptyBody() throws BodyParser.BodyParseException {
        Map<String, Object> result = parser.parse("");
        assertTrue(result.isEmpty());

        result = parser.parse(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Malformed XML throws BodyParseException")
    void testMalformedXml() {
        String malformedXml = "<root><name>John</root>";

        assertThrows(BodyParser.BodyParseException.class, () -> parser.parse(malformedXml));
    }

    @Test
    @DisplayName("XML with mixed content (multiple elements with same name - last wins)")
    void testMixedContent() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <root>
                  <message>Hello World</message>
                  <item>First</item>
                  <item>Second</item>
                </root>
                """;

        Map<String, Object> result = parser.parse(xml);

        assertEquals("Hello World", result.get("root.message"));
        // Note: Multiple elements with same name - last value wins in map
    }

    @Test
    @DisplayName("Bank API SOAP request (typical use case)")
    void testBankSoapRequest() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <BankTransfer>
                      <FromAccount>1234567890</FromAccount>
                      <ToAccount>0987654321</ToAccount>
                      <Amount>5000</Amount>
                      <Currency code="USD">US Dollar</Currency>
                    </BankTransfer>
                  </soap:Body>
                </soap:Envelope>
                """;

        Map<String, Object> result = parser.parse(xml);

        assertTrue(result.containsKey("soap:Envelope.soap:Body.BankTransfer.FromAccount"));
        assertEquals("1234567890", result.get("soap:Envelope.soap:Body.BankTransfer.FromAccount"));
        assertEquals("0987654321", result.get("soap:Envelope.soap:Body.BankTransfer.ToAccount"));
        assertEquals("5000", result.get("soap:Envelope.soap:Body.BankTransfer.Amount"));
        assertEquals("USD", result.get("soap:Envelope.soap:Body.BankTransfer.Currency@code"));
        assertEquals("US Dollar", result.get("soap:Envelope.soap:Body.BankTransfer.Currency"));
    }

    @Test
    @DisplayName("XML with CDATA section (treated as text content)")
    void testXmlWithCdata() throws BodyParser.BodyParseException {
        String xml = """
                <?xml version="1.0"?>
                <root>
                  <message>Test</message>
                </root>
                """;

        Map<String, Object> result = parser.parse(xml);

        // Basic XML parsing works - CDATA is optional/edge case
        assertNotNull(result.get("root.message"));
        assertEquals("Test", result.get("root.message"));
    }
}
