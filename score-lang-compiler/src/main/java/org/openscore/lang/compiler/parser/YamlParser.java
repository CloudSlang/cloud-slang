package org.openscore.lang.compiler.parser;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import org.apache.commons.lang3.Validate;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.compiler.parser.model.ParsedSlang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlParser {

    @Autowired
    private Yaml yaml;

    public ParsedSlang parse(SlangSource source) {

        Validate.notEmpty(source.getSource(), "Source " + source.getName() + " cannot be empty");

        try {
            ParsedSlang parsedSlang = yaml.loadAs(source.getSource(), ParsedSlang.class);
            if(parsedSlang == null) {
                throw new RuntimeException("Source " + source.getName() + " does not contain YAML content");
            }
            parsedSlang.setName(source.getName());
            return parsedSlang;
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " + source.getName() + ".\n" + e.getMessage(), e);
        }
    }

}
