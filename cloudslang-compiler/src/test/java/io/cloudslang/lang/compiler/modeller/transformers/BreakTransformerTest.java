package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.entities.ScoreLangConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BreakTransformerTest {

    @Test
    public void testTransformWithData() throws Exception {
        BreakTransformer breakTransformer = new BreakTransformer();
        List<String> list = Arrays.asList("1", "2");
        List<String> returnValue = breakTransformer.transform(list);
        Assert.assertEquals(list, returnValue);
    }

    @Test
    public void testTransformWithNullReturnDefault() throws Exception {
        BreakTransformer breakTransformer = new BreakTransformer();
        List<String> list = Arrays.asList(ScoreLangConstants.FAILURE_RESULT);
        List<String> returnValue = breakTransformer.transform(null);
        Assert.assertEquals(list, returnValue);
    }

    @Test
    public void testTransformWithEmptyListReturnEmptyList() throws Exception {
        BreakTransformer breakTransformer = new BreakTransformer();
        List<String> returnValue = breakTransformer.transform(new ArrayList<String>());
        List<String> expected = new ArrayList<>();
        Assert.assertEquals(expected, returnValue);
    }
}