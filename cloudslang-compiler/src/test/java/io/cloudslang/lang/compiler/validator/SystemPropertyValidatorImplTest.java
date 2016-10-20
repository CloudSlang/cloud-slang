/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Bonczidai Levente
 * @since 8/11/2016
 */
public class SystemPropertyValidatorImplTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SystemPropertyValidator systemPropertyValidator;

    public SystemPropertyValidatorImplTest() {
        systemPropertyValidator = new SystemPropertyValidatorImpl();
    }

    @Test
    public void testNamespaceEmpty() throws Exception {
        String input = "";
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceMatch1() throws Exception {
        String input = "a";
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceMatch2() throws Exception {
        String input = "a1b";
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceMatch3() throws Exception {
        String input = "a1b.c";
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceMatch4() throws Exception {
        String input = "a1b.c.dfe";
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceMatch5() throws Exception {
        String input = "a1b.123.dfe";
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceNoMatch1() throws Exception {
        String input = ".c";
        expectException("Error validating system property namespace. Nested exception is:" +
                " Argument[.c] cannot start with delimiter[.].");
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceNoMatch2() throws Exception {
        String input = "a..c";
        expectException("Error validating system property namespace. Nested exception is:" +
                " Argument[a..c] cannot contain multiple delimiters[.] without content.");
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceNoMatch3() throws Exception {
        String input = "a.c.";
        expectException("Error validating system property namespace. Nested exception is:" +
                " Argument[a.c.] cannot end with delimiter[.].");
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceNoMatch4() throws Exception {
        String input = ".";
        expectException("Error validating system property namespace." +
                " Nested exception is: Argument[.] cannot start with delimiter[.].");
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testNamespaceNoMatch5() throws Exception {
        String input = "a.b.?";
        expectException("Error validating system property namespace." +
                " Nested exception is: Argument[a.b.?] violates character rules.");
        systemPropertyValidator.validateNamespace(input);
    }

    @Test
    public void testKeyEmpty() throws Exception {
        String input = "";
        expectException("Key cannot be empty.");
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyMatch1() throws Exception {
        String input = "a";
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyMatch2() throws Exception {
        String input = "a1b";
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyMatch3() throws Exception {
        String input = "a1b.c";
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyMatch4() throws Exception {
        String input = "a1b.c.dfe";
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyMatch5() throws Exception {
        String input = "a1b.123.dfe";
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyNoMatch1() throws Exception {
        String input = ".c";
        expectException("Error validating system property key. Nested exception is:" +
                " Argument[.c] cannot start with delimiter[.].");
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyNoMatch2() throws Exception {
        String input = "a..c";
        expectException("Error validating system property key. Nested exception is: " +
                "Argument[a..c] cannot contain multiple delimiters[.] without content.");
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyNoMatch3() throws Exception {
        String input = "a.c.";
        expectException("Error validating system property key. Nested exception is:" +
                " Argument[a.c.] cannot end with delimiter[.].");
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyNoMatch4() throws Exception {
        String input = ".";
        expectException("Error validating system property key. Nested exception is:" +
                " Argument[.] cannot start with delimiter[.].");
        systemPropertyValidator.validateKey(input);
    }

    @Test
    public void testKeyNoMatch5() throws Exception {
        String input = "a.b.?";
        expectException("Error validating system property key. Nested exception is:" +
                " Argument[a.b.?] violates character rules.");
        systemPropertyValidator.validateKey(input);
    }

    private void expectException(String message) {
        exception.expect(RuntimeException.class);
        exception.expectMessage(message);
    }
}
