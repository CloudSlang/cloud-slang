package io.cloudslang.lang.runtime.configuration;

/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import io.cloudslang.lang.entities.SlangSystemPropertyConstant;
import io.cloudslang.runtime.impl.RuntimeManagementConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Options;
import org.python.core.PySystemState;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RuntimeManagementConfiguration.class})
@ComponentScan("io.cloudslang.lang.runtime")
public class SlangRuntimeSpringConfig {

    static {
        Options.importSite = false;
        setPythonIOEncoding();
    }

    private static void setPythonIOEncoding() {
        String encodingValue = System.getProperty(SlangSystemPropertyConstant.CSLANG_ENCODING.getValue());
        if (!StringUtils.isEmpty(encodingValue))
            System.getProperties().setProperty(PySystemState.PYTHON_IO_ENCODING, encodingValue);
    }
}
