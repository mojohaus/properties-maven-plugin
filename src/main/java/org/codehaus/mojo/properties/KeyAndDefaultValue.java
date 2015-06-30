package org.codehaus.mojo.properties;

/**
 * @author tdiamantis
 */
public class KeyAndDefaultValue {
    private String key;
    private String defaultValue;

    public KeyAndDefaultValue(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
