package org.codehaus.mojo.properties;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * A basic HOCON parser.
  */
public class HoconProperties {

  private boolean respectHierarchy;

  private HoconProperties(boolean respectHierarchy) {
    this.respectHierarchy = respectHierarchy;
  }

  // Loosely based on Slick's GlobalConfig#toProperties()
  // https://github.com/slick/slick/blob/3.1.0/slick/src/main/scala/slick/util/GlobalConfig.scala#L62
  private Properties parse(ConfigObject config) {

    Properties properties = new Properties();

    for (Map.Entry<String, ConfigValue> configEntry : config.entrySet()) {

      final Object value;

      if (configEntry.getValue().valueType().equals(ConfigValueType.OBJECT)) {
        value = parse((ConfigObject) configEntry.getValue());
      } else if (configEntry.getValue().unwrapped() == null) {
        value = null;
      } else {
        value = configEntry.getValue().unwrapped().toString();
      }

      if (!respectHierarchy && value != null && value instanceof Properties) {
        for (Map.Entry<Object, Object> e : ((Properties) value).entrySet()) {
          properties.put(configEntry.getKey() + "." + e.getKey(), e.getValue());
        }
      } else if (value != null) {
        properties.put(configEntry.getKey(), value);
      }
    }

    return properties;
  }

  public Properties parse(File file) {
    Config config = ConfigFactory.parseFile(file);
    return toProperties(config);
  }

  public Properties parse(String conf) {
    Config config = ConfigFactory.parseString(conf);
    return toProperties(config);
  }

  public Properties toProperties(Config conf) {
    return parse(conf.root());
  }

  public static HoconProperties respectHierarchy() {
    return new HoconProperties(true);
  }

  public static HoconProperties ignoreHierarchy() {
    return new HoconProperties(false);
  }
}
