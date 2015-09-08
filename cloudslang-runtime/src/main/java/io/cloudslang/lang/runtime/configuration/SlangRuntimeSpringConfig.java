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


import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Configuration
@ComponentScan("io.cloudslang.lang.runtime")
public class SlangRuntimeSpringConfig {

    static {
        Options.importSite = false;
    }

    @Bean
    public PythonInterpreter interpreter(){
        PythonInterpreter interpreter = new PythonInterpreter();
//        here to avoid jython preferring io.cloudslang package over python io package
        interpreter.exec("import io");
        return interpreter;
    }

    @Bean
    public ScriptEngine scriptEngine(){
        return  new ScriptEngineManager().getEngineByName("python");
    }

}
