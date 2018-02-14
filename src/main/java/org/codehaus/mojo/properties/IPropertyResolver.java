package org.codehaus.mojo.properties;

import java.util.Properties;

public interface IPropertyResolver {
    
    public String getPropertyValue( String key, Properties properties, Properties environment );
}
