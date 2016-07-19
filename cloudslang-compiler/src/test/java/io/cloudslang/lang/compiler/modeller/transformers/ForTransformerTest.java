package io.cloudslang.lang.compiler.modeller.transformers;

import junit.framework.Assert;
import io.cloudslang.lang.entities.ListForLoopStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapForLoopStatement;

public class ForTransformerTest extends TransformersTestParent {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ForTransformer transformer = new ForTransformer();

    public static ListForLoopStatement validateListForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof ListForLoopStatement);
        return (ListForLoopStatement) statement;
    }

    public static MapForLoopStatement validateMapForLoopStatement(LoopStatement statement) {
        Assert.assertEquals(true, statement instanceof MapForLoopStatement);
        return (MapForLoopStatement) statement;
    }

    @Test
    public void testValidStatement() throws Exception {
        LoopStatement statement = transformer.transform("x in collection").getTransformedData();
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("x", listForLoopStatement.getVarName());
        Assert.assertEquals("collection", listForLoopStatement.getExpression());
    }

    @Test
    public void testValidStatementWithSpaces() throws Exception {
        LoopStatement statement = transformer.transform("x in range(0, 9)").getTransformedData();
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("x", listForLoopStatement.getVarName());
        Assert.assertEquals("range(0, 9)", listForLoopStatement.getExpression());
    }

    @Test
    public void testValidStatementAndTrim() throws Exception {
        LoopStatement statement = transformer.transform(" min   in  collection  ").getTransformedData();
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("min", listForLoopStatement.getVarName());
        Assert.assertEquals("collection", listForLoopStatement.getExpression());
    }

    @Test
    public void testNoVarName() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        transformAndThrowFirstException(transformer, "  in  collection" );
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
        ListForLoopStatement listForLoopStatement  = validateListForLoopStatement(statement);
        Assert.assertEquals("in", listForLoopStatement.getExpression());
    }

    @Test
    public void testEmptyValue() throws Exception {
        LoopStatement statement = transformer.transform("").getTransformedData();
        Assert.assertNull(statement);
    }

    @Test
    public void testValidMapStatement() throws Exception {
        LoopStatement statement = transformer.transform("k, v in collection").getTransformedData();
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementSpaceBeforeComma() throws Exception {
        LoopStatement statement = transformer.transform("k ,v in collection").getTransformedData();
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementWithoutSpaceAfterComma() throws Exception {
        LoopStatement statement = transformer.transform("k,v in collection").getTransformedData();
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementAndTrim() throws Exception {
        LoopStatement statement = transformer.transform(" k, v   in  collection  ").getTransformedData();
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testValidMapStatementAndTrimMultipleWhitSpaces() throws Exception {
        LoopStatement statement = transformer.transform("   k,    v     in  collection  ").getTransformedData();
        MapForLoopStatement mapForLoopStatement  = validateMapForLoopStatement(statement);
        Assert.assertEquals("k", mapForLoopStatement.getKeyName());
        Assert.assertEquals("v", mapForLoopStatement.getValueName());
        Assert.assertEquals("collection", statement.getExpression());
    }

    @Test
    public void testMapVarNameContainInvalidChars() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("var name");
        exception.expectMessage("invalid");
        transformAndThrowFirstException(transformer, "(k v m)  in  collection" );
    }

    @Test
    public void testMapNoCollectionExpression() throws Exception {
        exception.expect(RuntimeException.class);
        exception.expectMessage("expression");
        transformAndThrowFirstException(transformer, "k, v in  ");
    }

}