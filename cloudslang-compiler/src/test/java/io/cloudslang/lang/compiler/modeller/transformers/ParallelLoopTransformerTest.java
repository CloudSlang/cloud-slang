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

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Date: 4/1/2015
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ParallelLoopTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class ParallelLoopTransformerTest extends TransformersTestParent {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ParallelLoopForTransformer transformer;

    @Test
    public void testValidStatement() throws Exception {
        ListLoopStatement statement = (ListLoopStatement) transformer.transform("x in collection").getTransformedData();
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        ListLoopStatement statement =
                (ListLoopStatement) transformer.transform("x in range(0, 9)").getTransformedData();
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("range(0, 9)", statement.getExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        ListLoopStatement statement =
                (ListLoopStatement) transformer.transform(" min   in  collection  ").getTransformedData();
        Assert.assertEquals("min", statement.getVarName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testNoVarName() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Argument[] violates character rules.");
        transformAndThrowFirstException(transformer, "  in  collection");
    }

    @Test
    public void testVarNameContainInvalidChars() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Argument[x a] violates character rules.");
        transformAndThrowFirstException(transformer, "x a  in  collection");
    }

    @Test
    public void testNoCollectionExpression() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("expression");
        transformAndThrowFirstException(transformer, "x in  ");
    }

    @Test
    public void testMultipleInsAreTrimmed() throws Exception {
        LoopStatement statement = transformer.transform(" in   in in ").getTransformedData();
        Assert.assertEquals("in", statement.getExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        LoopStatement statement = transformer.transform("").getTransformedData();
        Assert.assertNull(statement);
    }

    @Configuration
    public static class Config {
        @Bean
        public ParallelLoopForTransformer parallelLoopForTransformer() {
            return new ParallelLoopForTransformer();
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }
    }
}
