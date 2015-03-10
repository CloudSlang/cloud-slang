package org.openscore.lang.compiler.modeller.transformers;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openscore.lang.entities.ForLoopStatement;
import org.openscore.lang.entities.ListForLoopStatement;
import org.openscore.lang.entities.MapForLoopStatement;

public class ForTransformerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ForTransformer transformer = new ForTransformer();

    public static ListForLoopStatement validateListForLoopStatement(ForLoopStatement statement) {
        Assert.assertEquals(true, statement instanceof ListForLoopStatement);
        return (ListForLoopStatement) statement;
    }

    public static MapForLoopStatement validateMapForLoopStatement(ForLoopStatement statement) {
        Assert.assertEquals(true, statement instanceof MapForLoopStatement);
        return (MapForLoopStatement) statement;
    }

    @Test
    public void testValidStatement() throws Exception {
        ForLoopStatement statement = transformer.transform("x in collection");
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("x", listForLoopStatement.getVarName());
        Assert.assertEquals("collection", listForLoopStatement.getCollectionExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        ForLoopStatement statement = transformer.transform("x in range(0, 9)");
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("x", listForLoopStatement.getVarName());
        Assert.assertEquals("range(0, 9)", listForLoopStatement.getCollectionExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        ForLoopStatement statement = transformer.transform(" min   in  collection  ");
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("min", listForLoopStatement.getVarName());
        Assert.assertEquals("collection", listForLoopStatement.getCollectionExpression());
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
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("in", listForLoopStatement.getCollectionExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        ForLoopStatement statement = transformer.transform("");
        Assert.assertNull(statement);
    }

    @Test
    public void testValidMapStatement() throws Exception {
        ForLoopStatement statement = transformer.transform("k, v in collection");
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementSpaceBeforeComma() throws Exception {
        ForLoopStatement statement = transformer.transform("k ,v in collection");
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementWithoutSpaceAfterComma() throws Exception {
        ForLoopStatement statement = transformer.transform("k,v in collection");
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementWithExpression() throws Exception {
        ForLoopStatement statement = transformer.transform("k, v in dictionary.items()");
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("dictionary.items()", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementAndTrim() throws Exception {
        ForLoopStatement statement = transformer.transform(" k, v   in  collection  ");
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getCollectionExpression());
    }

    @Test
    public void testValidMapStatementAndTrimMultipleWhitSpaces() throws Exception {
        ForLoopStatement statement = transformer.transform("   k,    v     in  collection  ");
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
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