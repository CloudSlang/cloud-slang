/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.ParallelLoopStatement;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Date: 4/1/2015
 *
 * @author Bonczidai Levente
 */
public class ParallelLoopTransformerTest extends TransformersTestParent {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ParallelLoopForTransformer transformer = new ParallelLoopForTransformer();

    @Test
    public void testValidStatement() throws Exception {
        ParallelLoopStatement statement = (ParallelLoopStatement) transformer.transform("x in collection").getTransformedData();
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        ParallelLoopStatement statement = (ParallelLoopStatement) transformer.transform("x in range(0, 9)").getTransformedData();
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("range(0, 9)", statement.getExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        ParallelLoopStatement statement = (ParallelLoopStatement) transformer.transform(" min   in  collection  ").getTransformedData();
        Assert.assertEquals("min", statement.getVarName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testNoVarName() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        transformAndThrowFirstException(transformer, "  in  collection");
    }

    @Test
    public void testVarNameContainInvalidChars() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        exception.expectMessage("invalid");
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

}
