/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.reader.StreamReader;

public class CustomParserYaml extends Yaml {

    public <T> T loadAs(String yaml, Class<T> type) {
        @SuppressWarnings("unchecked") T object = (T) loadFromReader(new StreamReader(yaml), type);
        return object;
    }

    private Object loadFromReader(StreamReader sreader, Class<?> type) {
        Composer composer = new Composer(new ParserWithQuotesImpl(sreader), resolver);
        constructor.setComposer(composer);
        return constructor.getSingleData(type);
    }

}
