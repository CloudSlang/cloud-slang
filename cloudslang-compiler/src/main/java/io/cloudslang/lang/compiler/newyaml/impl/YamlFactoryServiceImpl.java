/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.newyaml.impl;

import io.cloudslang.lang.compiler.newyaml.YamlFactoryService;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import static io.cloudslang.lang.compiler.SlangTextualKeys.OBJECT_REPOSITORY_KEY;

public class YamlFactoryServiceImpl implements YamlFactoryService {

    @Override
    public Yaml createYamlForParsing() {
        Constructor constructor = new Constructor(ParsedSlang.class, new LoaderOptions());
        constructor.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                if (name.equals(OBJECT_REPOSITORY_KEY)) {
                    name = "objectRepository";
                }
                return super.getProperty(type, name);
            }
        });


        Yaml yaml = new Yaml(constructor);
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }

}
