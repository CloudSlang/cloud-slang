package io.cloudslang.lang.compiler.validator.matcher;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bonczidai Levente
 * @since 8/30/2016
 */
public class VariableNamePatternMatcherTest {
    private VariableNamePatternMatcher variableNamePatternMatcher;

    @Before
    public void setUp() throws Exception {
        variableNamePatternMatcher =  new VariableNamePatternMatcher();
    }

    @Test
    public void testNoMatch07() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a.b.c");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch08() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("Aa.b.c");
        assertFalse(matchResult);
    }

    @Test
    public void testMatch03() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch05StartsWithLetter() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch06StartsWithUnderscore() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("_varName");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch04() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("ABC");
        assertTrue(matchResult);
    }

    @Test
    public void testNoMatch10() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("0");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch11StartsWithNumber() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("69A");
        assertFalse(matchResult);
    }

    @Test
    public void testMatch07() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("_69A");
        assertTrue(matchResult);
    }

    @Test
    public void testNoMatch09() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("_69A-");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch01() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("$");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch02() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("$a");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch03() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a$");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch04() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a b");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch05() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a    b.c");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch06() throws Exception {
        boolean matchResult = variableNamePatternMatcher.matchesEndToEnd("a.b.c d");
        assertFalse(matchResult);
    }
}