package org.openscore.lang.compiler.modeller.transformers;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openscore.lang.compiler.modeller.model.LoopStatement;

public class ForTransformerTest {

    private ForTransformer transformer = new ForTransformer();

    @Test
    public void testValidStatement() throws Exception {
        LoopStatement statement = transformer.transform("x in collection");
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        LoopStatement statement = transformer.transform(" min   in  collection  ");
        Assert.assertEquals("min", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Ignore
    //what do we expect?
    @Test
    public void testMultipleIns() throws Exception {
        LoopStatement statement = transformer.transform(" in   in  collection  ");
        Assert.assertEquals("min", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        LoopStatement statement = transformer.transform("");
        Assert.assertNull(statement);
    }
}