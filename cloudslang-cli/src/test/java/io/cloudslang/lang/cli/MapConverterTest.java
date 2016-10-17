/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli;

import io.cloudslang.lang.cli.converters.MapConverter;

import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Date: 2/16/2015
 *
 * @author lesant
 */

public class MapConverterTest {

    public static final String VALID_INPUTS = "input1=value1,input2=value2";
    public static final String INVALID_INPUTS_WRONG_DELIMITER = "input1=value1;input2=value2";
    public static final String INVALID_INPUTS_CONCATENATED_VALUES = "input1=value1input2=value2";
    public static final String VALID_INPUTS_WITH_COMMA = "input1=value1\\,value2,input2=value3\\,value4";
    public static final String INVALID_INPUTS_UNESCAPED_COMMA = "input1=value1,value2,input2=value3,value4";
    public static final String INPUT_1 = "input1";
    public static final String INPUT_2 = "input2";
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MapConverter mapConverter;

    public MapConverterTest() {
        mapConverter = new MapConverter();
    }

    @Test
    public void testConvertSuccess() {
        Map<String, String> inputs = mapConverter.convertFromText(VALID_INPUTS, null, null);
        Assert.assertEquals("value1", inputs.get(INPUT_1));
        Assert.assertEquals("value2", inputs.get(INPUT_2));
    }

    @Test
    public void testConvertFailureWrongDelimiter() {
        expectException();

        Map<String, String> inputs = mapConverter.convertFromText(INVALID_INPUTS_WRONG_DELIMITER, null, null);
        Assert.assertEquals(null, inputs);

    }

    @Test
    public void testConvertFailureConcatenated() {
        expectException();

        Map<String, String> inputs = mapConverter.convertFromText(INVALID_INPUTS_CONCATENATED_VALUES, null, null);
        Assert.assertEquals(null, inputs);

    }

    @Test
    public void testConvertSuccessComma() {
        Map<String, String> inputs = mapConverter.convertFromText(VALID_INPUTS_WITH_COMMA, null, null);
        Assert.assertEquals("value1,value2", inputs.get(INPUT_1));
        Assert.assertEquals("value3,value4", inputs.get(INPUT_2));
    }

    @Test
    public void testConvertFailureComma() {
        expectException();

        Map<String, String> inputs = mapConverter.convertFromText(INVALID_INPUTS_UNESCAPED_COMMA, null, null);
        Assert.assertEquals(null, inputs);
    }

    private void expectException() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input");
        exception.expectMessage("key=value");
        exception.expectMessage("format");
    }


}
