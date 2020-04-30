package org.codehaus.mojo.properties;

import static org.junit.Assert.assertEquals;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

/**
 * A test class for the {@link HoconProperties} class.
 */
public class HoconPropertiesTest {

  @Test
  public void testParsingFlatConf() throws Exception {

    final String hoconf =
        "a=\"aValue\"\n"
        + "b=1\n"
        + "c=anotherVal\n";

    final Properties properties = HoconProperties.ignoreHierarchy().parse(hoconf);

    final int threeValues = 3;

    assertEquals(properties.size(), threeValues);
    assertEquals(properties.getProperty("a"), "aValue");
    assertEquals(properties.getProperty("b"), "1");
    assertEquals(properties.getProperty("c"), "anotherVal");
  }

  @Test
  public void testParsingHierarchicalConfWithIgnoreHierarchy() throws Exception {

    final String hoconf =
      "a {\n"
          + "  b {\n"
          + "    c.d = aValue\n"
          + "  }\n"
          + "}\n";

    final Properties properties = HoconProperties.ignoreHierarchy().parse(hoconf);

    final int oneValue = 1;
    assertEquals(properties.size(), oneValue);
    assertEquals(properties.getProperty("a.b.c.d"), "aValue");
  }

  @Test
  public void testParsingHierarchicalConfWithRespectHierarchy() throws Exception {

    final String hoconf =
        "a {\n"
            + "  b {\n"
            + "    c.d = aValue\n"
            + "  }\n"
            + "}\n";

    final Properties properties = HoconProperties.respectHierarchy().parse(hoconf);

    final Properties a = (Properties) properties.get("a");
    final Properties b = (Properties) a.get("b");
    final Properties c = (Properties) b.get("c");

    final int oneValue = 1;
    assertEquals(properties.size(), oneValue);
    assertEquals(a.size(), oneValue);
    assertEquals(b.size(), oneValue);
    assertEquals(c.size(), oneValue);
    assertEquals(c.getProperty("d"), "aValue");
  }

  @Test
  public void testParsingWithUnResolvablePlaceHolder() throws Exception {

    final String hoconf =
            "a=${aValue}\n"
            + "b=1\n"
            + "c=anotherVal\n";

    final Properties properties = HoconProperties.ignoreHierarchy().parse(hoconf);

    final int twoValues = 2;

    assertEquals(properties.size(), twoValues);
    assertEquals(properties.getProperty("b"), "1");
    assertEquals(properties.getProperty("c"), "anotherVal");
  }

  @Test
  public void testParsingWithResolvablePlaceHolder() throws Exception {

    final String hoconf =
        "aValue = bValue\n"
            + "a=${aValue}\n"
            + "b=1\n"
            + "c=anotherVal\n";

    final Properties properties = HoconProperties.ignoreHierarchy().parse(hoconf);

    final int fourValues = 4;

    assertEquals(properties.size(), fourValues);
    assertEquals(properties.getProperty("aValue"), "bValue");
    assertEquals(properties.getProperty("a"), "bValue");
    assertEquals(properties.getProperty("b"), "1");
    assertEquals(properties.getProperty("c"), "anotherVal");
  }
}
