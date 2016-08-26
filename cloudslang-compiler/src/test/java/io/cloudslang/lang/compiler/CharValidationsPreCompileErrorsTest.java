/*
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import java.net.URI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * @author Bonczidai Levente
 * @since 8/26/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CharValidationsPreCompileErrorsTest {
    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNamespace() throws Exception {
        runAndValidateError("/corrupted/chars/op_1.sl", "Argument[bo$$.ops] contains invalid characters.");
    }

    @Test
    public void testImportValue() throws Exception {
        runAndValidateError("/corrupted/chars/flow_1.sl", "Argument[bo$$.ops] contains invalid characters.");
    }

    @Test
    public void testReferenceID() throws Exception {
        runAndValidateError("/corrupted/chars/flow_2.sl", "Argument[bo$$.ops.check_Weather] contains invalid characters.");
    }

    @Test
    public void testAlias() throws Exception {
        runAndValidateError("/corrupted/chars/flow_3.sl", "Argument[op$] contains invalid characters.");
    }

    public void runAndValidateError(String sourcePath, String... messages) throws Exception {
        URI resource = getClass().getResource(sourcePath).toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        if (messages.length > 0) {
            exception.expect(RuntimeException.class);
            for (String element : messages) {
                exception.expectMessage(element);
            }
        }
        throw result.getErrors().get(0);
    }
}
