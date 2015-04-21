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

import io.cloudslang.lang.entities.AsyncLoopStatement;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Date: 4/1/2015
 *
 * @author Bonczidai Levente
 */
public class AsyncLoopTransformerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private AsyncLoopForTransformer transformer = new AsyncLoopForTransformer();

    @Test
    public void testValidStatement() throws Exception {
        AsyncLoopStatement statement = transformer.transform("x in collection");
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        AsyncLoopStatement statement = transformer.transform("x in range(0, 9)");
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("range(0, 9)", statement.getExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        AsyncLoopStatement statement = transformer.transform(" min   in  collection  ");
        Assert.assertEquals("min", statement.getVarName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testNoVarName() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        transformer.transform("  in  collection");
    }

    @Test
    public void testVarNameContainInvalidChars() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        exception.expectMessage("invalid");
        transformer.transform("x a  in  collection");
    }

    @Test
    public void testNoCollectionExpression() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("expression");
        transformer.transform("x in  ");
    }

    @Test
    public void testMultipleInsAreTrimmed() throws Exception {
        AsyncLoopStatement statement = transformer.transform(" in   in in ");
        Assert.assertEquals("in", statement.getExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        AsyncLoopStatement statement = transformer.transform("");
        Assert.assertNull(statement);
    }

}
