package org.openscore.lang.compiler.modeller.transformers;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openscore.lang.entities.ForLoopStatement;

import java.io.Serializable;

public class ForTransformerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ForTransformer transformer = new ForTransformer();

    @Test
    public void testValidStatement() throws Exception {
        ForLoopStatement statement = transformer.transform("x in collection");
        Assert.assertEquals(ForLoopStatement.Type.LIST, statement.getType());
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        ForLoopStatement statement = transformer.transform("x in range(0, 9)");
        Assert.assertEquals(ForLoopStatement.Type.LIST, statement.getType());
        Assert.assertEquals("x", statement.getVarName());
        Assert.assertEquals("range(0, 9)", statement.getCollectionExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        ForLoopStatement statement = transformer.transform(" min   in  collection  ");
        Assert.assertEquals(ForLoopStatement.Type.LIST, statement.getType());
        Assert.assertEquals("min", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
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
        exception.expectMessage("collection expression");
        transformer.transform("x in  ");
    }

    @Test
    public void testMultipleInsAreTrimmed() throws Exception {
        ForLoopStatement statement = transformer.transform(" in   in in ");
        Assert.assertEquals("in", statement.getVarName());
        Assert.assertEquals("in", statement.getCollectionExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        ForLoopStatement statement = transformer.transform("");
        Assert.assertNull(statement);
    }

    @Test
    public void testValidMapStatement() throws Exception {
        ForLoopStatement statement = transformer.transform("k, v in collection");
        Assert.assertEquals(ForLoopStatement.Type.MAP, statement.getType());
        Assert.assertEquals("k v", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementSpaceBeforeComma() throws Exception {
        ForLoopStatement statement = transformer.transform("k ,v in collection");
        Assert.assertEquals(ForLoopStatement.Type.MAP, statement.getType());
        Assert.assertEquals("k v", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementWithoutSpaceAfterComma() throws Exception {
        ForLoopStatement statement = transformer.transform("k,v in collection");
        Assert.assertEquals(ForLoopStatement.Type.MAP, statement.getType());
        Assert.assertEquals("k v", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementWithExpression() throws Exception {
        ForLoopStatement statement = transformer.transform("k, v in dictionary.items()");
        Assert.assertEquals(ForLoopStatement.Type.MAP, statement.getType());
        Assert.assertEquals("k v", statement.getVarName());
        Assert.assertEquals("dictionary.items()", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementAndTrim() throws Exception {
        ForLoopStatement statement = transformer.transform(" k, v   in  collection  ");
        Assert.assertEquals(ForLoopStatement.Type.MAP, statement.getType());
        Assert.assertEquals("k v", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementAndTrimMultipleWhitSpaces() throws Exception {
        ForLoopStatement statement = transformer.transform("   k,    v     in  collection  ");
        Assert.assertEquals(ForLoopStatement.Type.MAP, statement.getType());
        Assert.assertEquals("k v", statement.getVarName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testMapVarNameContainInvalidChars() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        exception.expectMessage("invalid");
        transformer.transform("(k v m)  in  collection");
    }

    @Test
    public void testMapNoCollectionExpression() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("collection expression");
        transformer.transform("k, v in  ");
    }

}