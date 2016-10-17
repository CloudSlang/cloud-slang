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

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Bonczidai Levente
 * @since 5/5/2016
 */
public class AbstractTransformerTest extends TransformersTestParent {

    @Rule
    public ExpectedException exception = ExpectedException.none();
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
        exception.expect(IllegalArgumentException.class);
        abstractTransformer.validateKeySet(null, null, null);
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
        exception.expect(RuntimeException.class);
        exception.expectMessage(AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_PREFIX);
        exception.expectMessage(invalidKey1);
        abstractTransformer.validateKeySet(
                Sets.newHashSet(mandatoryKey1, mandatoryKey2, invalidKey1),
                mandatoryKeys,
                optionalKeys
        );
    }

    @Test
    public void testMissingKeys() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage(AbstractTransformer.MISSING_KEYS_ERROR_MESSAGE_PREFIX);
        exception.expectMessage(mandatoryKey2);
        abstractTransformer.validateKeySet(
                Sets.newHashSet(mandatoryKey1, optionalKey1),
                mandatoryKeys,
                optionalKeys
        );
    }

}