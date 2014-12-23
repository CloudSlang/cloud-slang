package org.openscore.lang.compiler.utils;
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

import org.openscore.lang.compiler.model.SlangFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;

@Component
public class YamlParser {

    @Autowired
    private Yaml yaml;

    public SlangFile loadSlangFile(File source) {
        SlangFile slangFile;
        try (FileInputStream is = new FileInputStream(source)) {
            slangFile = yaml.loadAs(is, SlangFile.class);
            slangFile.setFileName(source.getName());
        } catch (java.io.IOException e) {
            throw new RuntimeException("There was a problem parsing the yaml file: " + source.getName() + " syntax for some reason", e);
        }
        return slangFile;
    }

}
