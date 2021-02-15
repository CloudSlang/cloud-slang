/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static io.cloudslang.lang.compiler.modeller.transformers.AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX;
import static io.cloudslang.lang.compiler.modeller.transformers.AbstractTransformer.MISSING_KEYS_ERROR_MESSAGE_PREFIX;
import static org.junit.Assert.assertThrows;

/**
 * @author Bonczidai Levente
 * @since 5/5/2016
 */
public class AbstractTransformerTest extends TransformersTestParent {

    private AbstractTransformer abstractTransformer;
    private Set<String> mandatoryKeys;
    private Set<String> optionalKeys;
    private String mandatoryKey1;
    private String mandatoryKey2;
    private String optionalKey1;
    private String invalidKey1;

    @Before
    public void setUp() {
        abstractTransformer = new AbstractTransformer() {
        };
        mandatoryKey1 = "mandatory1";
        mandatoryKey2 = "mandatory2";
        optionalKey1 = "optional1";
        invalidKey1 = "invalid1";
        mandatoryKeys = Sets.newHashSet(mandatoryKey1, mandatoryKey2);
        optionalKeys = Sets.newHashSet(optionalKey1);
    }

    @Test
    public void testNulls() throws Exception {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                abstractTransformer.validateKeySet(null, null, null));
        Assert.assertEquals("The validated object is null", exception.getMessage());
    }

    @Test
    public void testMandatoryKeys() throws Exception {
        abstractTransformer.validateKeySet(
                Sets.newHashSet(mandatoryKey1, mandatoryKey2),
                mandatoryKeys,
                optionalKeys
        );
    }

    @Test
    public void testMandatoryAndOptionalKeys() throws Exception {
        abstractTransformer.validateKeySet(
                Sets.newHashSet(mandatoryKey1, mandatoryKey2, optionalKey1),
                mandatoryKeys,
                optionalKeys
        );
    }

    @Test
    public void testInvalidKeys() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                abstractTransformer.validateKeySet(
                        Sets.newHashSet(mandatoryKey1, mandatoryKey2, invalidKey1),
                        mandatoryKeys,
                        optionalKeys
                ));
        Assert.assertTrue(exception.getMessage().contains(INVALID_KEYS_ERROR_MESSAGE_PREFIX));
        Assert.assertTrue(exception.getMessage().contains(invalidKey1));

    }

    @Test
    public void testMissingKeys() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                abstractTransformer.validateKeySet(
                        Sets.newHashSet(mandatoryKey1, optionalKey1),
                        mandatoryKeys,
                        optionalKeys
                ));
        Assert.assertTrue(exception.getMessage().contains(MISSING_KEYS_ERROR_MESSAGE_PREFIX));
        Assert.assertTrue(exception.getMessage().contains(mandatoryKey2));
    }

}