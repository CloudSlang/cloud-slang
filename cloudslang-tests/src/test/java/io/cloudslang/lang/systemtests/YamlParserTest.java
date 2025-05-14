/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.compiler.parser.YamlParser;
import org.yaml.snakeyaml.Yaml;

public class YamlParserTest extends YamlParser {

    private Yaml yaml;

    @Override
    protected Yaml getYaml() {
        return yaml;
    }

    public void setYaml(Yaml yaml) {
        this.yaml = yaml;
    }
}
