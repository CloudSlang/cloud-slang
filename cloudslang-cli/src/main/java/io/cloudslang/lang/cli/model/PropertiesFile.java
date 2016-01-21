/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.cli.model;

import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 1/20/2016
 */
public class PropertiesFile {

    public static final String PROPERTIES_KEY = "properties";

    private String namespace;
    private Map<String, String> properties;

    public String getNamespace() {
        return namespace;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

}
