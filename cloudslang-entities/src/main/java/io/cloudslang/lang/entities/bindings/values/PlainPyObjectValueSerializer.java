/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.values;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serializable;

public class PlainPyObjectValueSerializer extends StdSerializer<PlainPyObjectValue> {
    private static final long serialVersionUID = 2191711615391896498L;

    public PlainPyObjectValueSerializer() {
        this(null);
    }

    public PlainPyObjectValueSerializer(Class<PlainPyObjectValue> t) {
        super(t);
    }

    @Override
    public void serialize(PlainPyObjectValue plainPyObjectValue, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        Serializable content = plainPyObjectValue.get();
        jsonGenerator.writeString(content == null ? "" : content.toString());
    }
}
