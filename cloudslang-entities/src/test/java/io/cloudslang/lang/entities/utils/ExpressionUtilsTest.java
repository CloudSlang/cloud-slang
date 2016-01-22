package io.cloudslang.lang.entities.utils;

import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static io.cloudslang.lang.entities.utils.ExpressionUtils.extractExpression;
import static io.cloudslang.lang.entities.utils.ExpressionUtils.extractSystemProperties;
import static io.cloudslang.lang.entities.utils.ExpressionUtils.matchGetFunction;

/**
 * @author Bonczidai Levente
 * @since 1/22/2016
 */
public class ExpressionUtilsTest {

    @SuppressWarnings("unchecked")
    private static final Set<String> EMPTY_SET = (Set<String>) Collections.EMPTY_SET;
    private Set<String> props1 = Sets.newHashSet("a.b.c.key");
    private Set<String> props2 = Sets.newHashSet("a.b.c.key", "d.e.f.key");

    @Test
    public void testExtractExpressionNonString() throws Exception {
        Assert.assertEquals(null, extractExpression(5));
    }

    @Test
    public void testExtractExpressionString() throws Exception {
        Assert.assertEquals(null, extractExpression("abc"));
    }

    @Test
    public void testExtractExpressionNoMatch() throws Exception {
        Assert.assertEquals(null, extractExpression("${var{"));
    }

    @Test
    public void testExtractExpressionAssignFrom() throws Exception {
        Assert.assertEquals("var", extractExpression("${var}"));
    }

    @Test
    public void testExtractExpressionConcat() throws Exception {
        Assert.assertEquals("var + 'abc'", extractExpression("${var + 'abc'}"));
    }

    @Test
    public void testExtractExpressionWhitespacesAreKept() throws Exception {
        Assert.assertEquals("   var + 'abc'      ", extractExpression("${   var + 'abc'      }"));
    }

    @Test
    public void testExtractSystemPropertiesNoMatch() throws Exception {
        Assert.assertEquals(EMPTY_SET, extractSystemProperties("get_sys('a.b.c.key')"));
    }

    @Test
    public void testExtractSystemPropertiesSingleMatch() throws Exception {
        Assert.assertEquals(props1, extractSystemProperties("get_sp('a.b.c.key')"));
    }

    @Test
    public void testExtractSystemPropertiesWhitespaces() throws Exception {
        Assert.assertEquals(props1, extractSystemProperties("get_sp( 'a.b.c.key'              )"));
    }

    @Test
    public void testExtractSystemPropertiesNoMatchExpression() throws Exception {
        Assert.assertEquals(EMPTY_SET, extractSystemProperties("get_sp('a.b.c.key' + var)"));
    }

    @Test
    public void testExtractSystemPropertiesNoMatchConcat() throws Exception {
        Assert.assertEquals(EMPTY_SET, extractSystemProperties("get_sp('a.b.c.key' + 'hello')"));
    }

    @Test
    public void testExtractSystemPropertiesNoMatchGet() throws Exception {
        Assert.assertEquals(EMPTY_SET, extractSystemProperties("get_sp(get('key'))"));
    }

    @Test
    public void testExtractSystemPropertiesDefault() throws Exception {
        Assert.assertEquals(props1, extractSystemProperties("get_sp('a.b.c.key', default_expression)"));
    }

    @Test
    public void testExtractSystemPropertiesDefaultWhitespaces() throws Exception {
        Assert.assertEquals(props1, extractSystemProperties("get_sp( 'a.b.c.key' ,       default_expression)"));
    }

    @Test
    public void testExtractSystemPropertiesMixed() throws Exception {
        Assert.assertEquals(props2, extractSystemProperties("get(get_sp('a.b.c.key'), get_sp('d.e.f.key', default_expression))"));
    }

    @Test
    public void testMatchGetFunctionString() throws Exception {
        Assert.assertFalse(matchGetFunction("hello"));
    }

    @Test
    public void testMatchGetFunctionNoMatch() throws Exception {
        Assert.assertFalse(matchGetFunction("get(expression)"));
    }

    @Test
    public void testMatchGetFunctionFalsePositive() throws Exception {
        Assert.assertTrue(matchGetFunction("get('a', var, 'too_many_params')"));
    }

    @Test
    public void testMatchGetFunctionMatch() throws Exception {
        Assert.assertTrue(matchGetFunction("get('a', var)"));
    }

    @Test
    public void testMatchGetFunctionWhitespaces() throws Exception {
        Assert.assertTrue(matchGetFunction("get( 'a'        ,  var)"));
    }

    @Test
    public void testMatchGetFunctionMatchAtLeastOnce() throws Exception {
        Assert.assertTrue(matchGetFunction("exec('a', get(get(var, default_expr), default_expr))"));
    }

}
