package io.cloudslang.lang.compiler.validator.matcher;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bonczidai Levente
 * @since 8/30/2016
 */
public class NamespacePatternMatcherTest {
    private NamespacePatternMatcher namespacePatternMatcher;

    @Before
    public void setUp() throws Exception {
        namespacePatternMatcher=  new NamespacePatternMatcher();
    }

    @Test
    public void testMatch01() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("a.b.c");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch02() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("Aa.b.c");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch03() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("a");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch04() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("ABC");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch05() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("0");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch06() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("69A");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch07() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("_69A");
        assertTrue(matchResult);
    }

    @Test
    public void testMatch08() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("_69A-");
        assertTrue(matchResult);
    }

    @Test
    public void testNoMatch01() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("$");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch02() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("$a");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch03() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("a$");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch04() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("a b");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch05() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("a    b.c");
        assertFalse(matchResult);
    }

    @Test
    public void testNoMatch06() throws Exception {
        boolean matchResult = namespacePatternMatcher.matchesEndToEnd("a.b.c d");
        assertFalse(matchResult);
    }
}