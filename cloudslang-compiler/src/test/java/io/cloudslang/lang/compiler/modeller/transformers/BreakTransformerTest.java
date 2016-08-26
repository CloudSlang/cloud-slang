package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.ScoreLangConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BreakTransformerTest.Config.class)
public class BreakTransformerTest extends TransformersTestParent {

    @Autowired
    private BreakTransformer breakTransformer;

    @Test
    public void testTransformWithData() throws Exception {
        List<String> list = Arrays.asList("1", "2");
        List<String> returnValue = breakTransformer.transform(list).getTransformedData();
        Assert.assertEquals(list, returnValue);
    }

    @Test
    public void testTransformWithNullReturnDefault() throws Exception {
        List<String> list = Arrays.asList(ScoreLangConstants.FAILURE_RESULT);
        List<String> returnValue = breakTransformer.transform(null).getTransformedData();
        Assert.assertEquals(list, returnValue);
    }

    @Test
    public void testTransformWithEmptyListReturnEmptyList() throws Exception {
        List<String> returnValue = breakTransformer.transform(new ArrayList<String>()).getTransformedData();
        List<String> expected = new ArrayList<>();
        Assert.assertEquals(expected, returnValue);
    }

    @Configuration
    public static class Config {
        @Bean
        public BreakTransformer breakTransformer() {
            return new BreakTransformer();
        }
        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }
        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }
    }

}