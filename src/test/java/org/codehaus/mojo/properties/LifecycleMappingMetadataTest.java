package org.codehaus.mojo.properties;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the m2e lifecycle mapping in step with the goals the plugin descriptor declares.
 */
class LifecycleMappingMetadataTest {

    private static Document parse(String resource) throws Exception {
        try (InputStream in = LifecycleMappingMetadataTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource + " is not on the classpath");
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        }
    }

    private static Set<String> texts(Document document, String tag, Element scope) {
        Set<String> result = new LinkedHashSet<>();
        NodeList nodes = (scope != null ? scope : document.getDocumentElement()).getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i).getTextContent().trim());
        }
        return result;
    }

    @Test
    void everyGoalIsMappedToExecuteOnIncrementalAndConfiguration() throws Exception {
        Document descriptor = parse("/META-INF/maven/plugin.xml");
        Set<String> declaredGoals = new LinkedHashSet<>();
        NodeList mojos = descriptor.getDocumentElement().getElementsByTagName("mojo");
        for (int i = 0; i < mojos.getLength(); i++) {
            Element mojo = (Element) mojos.item(i);
            for (Node child = mojo.getFirstChild(); child != null; child = child.getNextSibling()) {
                if ("goal".equals(child.getNodeName())) {
                    declaredGoals.add(child.getTextContent().trim());
                }
            }
        }
        // the generated help goal changes nothing in the project
        declaredGoals.remove("help");
        assertTrue(declaredGoals.size() >= 4, "unexpected plugin descriptor: " + declaredGoals);

        Document mapping = parse("/META-INF/m2e/lifecycle-mapping-metadata.xml");
        Set<String> mappedGoals = new LinkedHashSet<>();
        NodeList executions = mapping.getElementsByTagName("pluginExecution");
        for (int i = 0; i < executions.getLength(); i++) {
            Element execution = (Element) executions.item(i);
            Set<String> goals = texts(mapping, "goal", execution);
            mappedGoals.addAll(goals);
            assertEquals(
                    "[true]", texts(mapping, "runOnIncremental", execution).toString(), "runOnIncremental of " + goals);
            assertEquals(
                    "[true]",
                    texts(mapping, "runOnConfiguration", execution).toString(),
                    "runOnConfiguration of " + goals);
        }

        assertEquals(declaredGoals, mappedGoals);
    }
}
