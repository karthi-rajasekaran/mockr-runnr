package com.mockr.runnr.matcher.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses XML and SOAP bodies with namespace and attribute support.
 * 
 * Used for SOAP APIs and XML-based services (e.g., banking APIs).
 * 
 * Features:
 * - Preserves namespace prefixes in keys: soap:Body, ns:Element
 * - Stores attributes with @ prefix: Element@version="1.0"
 * - Recursively flattens child elements with dot notation
 * 
 * Example:
 * <soap:Body>
 * <TransferRequest version="1.0">
 * <From>ACC123</From>
 * </TransferRequest>
 * </soap:Body>
 * → {
 * soap:Body.TransferRequest@version: "1.0",
 * soap:Body.TransferRequest.From: "ACC123"
 * }
 */
public class XmlBodyParser implements BodyParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlBodyParser.class);
    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    static {
        try {
            // Disable external entity resolution for security
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (Exception e) {
            logger.warn("Could not configure XML parser security features", e);
        }
    }

    @Override
    public Map<String, Object> parse(String body) throws BodyParseException {
        if (body == null || body.isBlank()) {
            return new HashMap<>();
        }

        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(body));
            Document doc = builder.parse(source);

            return flattenXml(doc.getDocumentElement(), "");
        } catch (Exception e) {
            throw new BodyParseException("Failed to parse XML body", e);
        }
    }

    /**
     * Recursively flatten XML element and its children.
     * 
     * @param elem   Element to flatten
     * @param prefix Current path prefix
     * @return Flattened map
     */
    private Map<String, Object> flattenXml(Element elem, String prefix) {
        Map<String, Object> result = new HashMap<>();

        String nodeName = elem.getTagName();
        String currentPath = prefix.isEmpty() ? nodeName : prefix + "." + nodeName;

        // Add attributes with @ suffix: Element@version="1.0"
        NamedNodeMap attrs = elem.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            result.put(currentPath + "@" + attr.getName(), attr.getValue());
        }

        // Recursively flatten child elements and text nodes
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child instanceof Element) {
                result.putAll(flattenXml((Element) child, currentPath));
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getNodeValue().trim();
                if (!text.isEmpty()) {
                    result.put(currentPath, text);
                }
            }
        }

        return result;
    }
}
