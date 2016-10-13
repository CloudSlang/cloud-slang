/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.api;

import io.cloudslang.lang.entities.bindings.values.Value;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 8/23/2016
 */
public interface SlangSourceService {
    Map<String, Value> convertInputFromMap(Map<String, ? extends Serializable> rawMap, String artifact);
}
