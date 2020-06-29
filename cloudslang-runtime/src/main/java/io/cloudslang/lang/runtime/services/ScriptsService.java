/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.services;

import io.cloudslang.lang.entities.bindings.ScriptFunction;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScriptsService {

    private static final Logger logger = Logger.getLogger(ScriptsService.class);

    private Map<ScriptFunction, String> scriptsMap = new HashMap<>();

    @PostConstruct
    public void init() {
        List<ScriptFunction> list = Arrays.asList(
                ScriptFunction.GET,
                ScriptFunction.GET_SYSTEM_PROPERTY,
                ScriptFunction.CHECK_EMPTY,
                ScriptFunction.CS_APPEND,
                ScriptFunction.CS_PREPEND,
                ScriptFunction.CS_EXTRACT_NUMBER,
                ScriptFunction.CS_REPLACE,
                ScriptFunction.CS_ROUND,
                ScriptFunction.CS_SUBSTRING,
                ScriptFunction.CS_TO_UPPER,
                ScriptFunction.CS_TO_LOWER
        );

        for (ScriptFunction function: list) {
            String script = loadScript(function.getValue());
            scriptsMap.put(function, script);
        }
    }

    private String loadScript(String script) {
        String resourceFilePath = "/scripts/" + script + ".py";

        try (InputStream inputStream = this.getClass().getResourceAsStream(resourceFilePath)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        } catch (IOException e) {
            logger.error("Unable to read properties from " + resourceFilePath, e);
        }

        return "";
    }

    public String getScript(ScriptFunction function) {
        return scriptsMap.get(function);
    }
}
