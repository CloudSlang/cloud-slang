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
import io.cloudslang.lang.entities.MapLoopStatement;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ForTransformerTest.Config.class, SlangCompilerSpringConfig.class})
public class ForTransformerTest extends TransformersTestParent {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ForTransformer transformer;

    public static ListLoopStatement validateListForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof ListLoopStatement);
        return (ListLoopStatement) statement;
    }

    public static MapLoopStatement validateMapForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof MapLoopStatement);
        return (MapLoopStatement) statement;
    }

    @Test
    public void testValidStatement() throws Exception {
        LoopStatement statement = transformer.transform("x in collection").getTransformedData();
        ListLoopStatement listLoopStatement = validateListForLoopStatement(statement);
        Assert.assertEquals("x", listLoopStatement.getVarName());
        Assert.assertEquals("collection", listLoopStatement.getExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        LoopStatement statement = transformer.transform("x in range(0, 9)").getTransformedData();
        ListLoopStatement listLoopStatement = validateListForLoopStatement(statement);
        Assert.assertEquals("x", listLoopStatement.getVarName());
        Assert.assertEquals("range(0, 9)", listLoopStatement.getExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        LoopStatement statement = transformer.transform(" min   in  collection  ").getTransformedData();
        ListLoopStatement listLoopStatement = validateListForLoopStatement(statement);
        Assert.assertEquals("min", listLoopStatement.getVarName());
        Assert.assertEquals("collection", listLoopStatement.getExpression());
    }

    @Test
    public void testNoVarName() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Argument[] violates character rules");
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
        ListLoopStatement listLoopStatement = validateListForLoopStatement(statement);
        Assert.assertEquals("in", listLoopStatement.getExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        LoopStatement statement = transformer.transform("").getTransformedData();
        Assert.assertNull(statement);
    }

    @Test
    public void testValidMapStatement() throws Exception {
        LoopStatement statement = transformer.transform("k, v in collection").getTransformedData();
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapLoopStatement.getKeyName());
        Assert.assertEquals("v", mapLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementSpaceBeforeComma() throws Exception {
        LoopStatement statement = transformer.transform("k ,v in collection").getTransformedData();
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapLoopStatement.getKeyName());
        Assert.assertEquals("v", mapLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementWithoutSpaceAfterComma() throws Exception {
        LoopStatement statement = transformer.transform("k,v in collection").getTransformedData();
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapLoopStatement.getKeyName());
        Assert.assertEquals("v", mapLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementAndTrim() throws Exception {
        LoopStatement statement = transformer.transform(" k, v   in  collection  ").getTransformedData();
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapLoopStatement.getKeyName());
        Assert.assertEquals("v", mapLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementAndTrimMultipleWhitSpaces() throws Exception {
        LoopStatement statement = transformer.transform("   k,    v     in  collection  ").getTransformedData();
        MapLoopStatement mapLoopStatement = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapLoopStatement.getKeyName());
        Assert.assertEquals("v", mapLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testMapVarNameContainInvalidChars() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Argument[(k v m)] violates character rules.");
        transformAndThrowFirstException(transformer, "(k v m)  in  collection");
    }

    @Test
    public void testMapNoCollectionExpression() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("expression");
        transformAndThrowFirstException(transformer, "k, v in  ");
    }

    @Configuration
    public static class Config {
        @Bean
        public ForTransformer forTransformer() {
            return new ForTransformer();
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