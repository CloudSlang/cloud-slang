/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.newyaml;


import org.vibur.objectpool.PoolObjectFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.function.Supplier;

public class ViburYamlFactory implements PoolObjectFactory<Yaml> {

    private final Supplier<Yaml> yamlSupplier;

    public ViburYamlFactory(Supplier<Yaml> supplier) {
        this.yamlSupplier = supplier;
    }

    @Override
    public Yaml create() {
        return yamlSupplier.get();
    }

    @Override
    public boolean readyToTake(Yaml objectMapper) {
        return true;
    }

    @Override
    public boolean readyToRestore(Yaml objectMapper) {
        return true;
    }

    @Override
    public void destroy(Yaml objectMapper) {
    }

}
