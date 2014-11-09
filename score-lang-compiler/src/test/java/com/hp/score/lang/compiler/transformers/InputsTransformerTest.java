package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.utils.YamlParser;
import com.hp.score.lang.entities.bindings.Input;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class InputsTransformerTest {

    @Autowired
    private Transformer inputTransformer;

    @Autowired
    private YamlParser yamlParser;

    private Object inputsMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/operation_with_data.yaml");
        SlangFile file = yamlParser.loadMomaFile(new File(resource.toURI()));
        Map op =  file.getOperations().iterator().next();
        Map<String,Object> opProp = (Map)op.get("test_op");
        inputsMap = opProp.get("inputs");
    }

    @Test
    public void testTransform() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Assert.assertFalse(inputs.isEmpty());
    }

    @Test
     public void testSimpleRefInput() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Input input = inputs.get(0);
        Assert.assertEquals("input1",input.getName());
        Assert.assertEquals("input1",input.getExpression());
    }

    @Test
    public void testExplicitRefInput() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Input input = inputs.get(1);
        Assert.assertEquals("input2",input.getName());
        Assert.assertEquals("input2",input.getExpression());
    }

    @Test
    public void testDefaultValueInput() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Input input = inputs.get(2);
        Assert.assertEquals("input3",input.getName());
        Assert.assertEquals("value3",input.getDefaultValue());
    }

    @Test
    public void testInlineExprInput() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Input input = inputs.get(3);
        Assert.assertEquals("input4",input.getName());
        Assert.assertEquals("'value4' if input3 == value3 else None",input.getExpression());
    }

    @Test
    public void testReqEncInput() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Input input = inputs.get(4);
        Assert.assertEquals("input5",input.getName());
        Assert.assertEquals("input5",input.getExpression());
        Assert.assertEquals(true,input.isEncrypted());
        Assert.assertEquals(true,input.isRequired());
    }

    @Test
    public void testDefaultExprReqInput() throws Exception {
        List<Input> inputs = (List<Input>)inputTransformer.transform(inputsMap);
        Input input = inputs.get(5);
        Assert.assertEquals("input6",input.getName());
        Assert.assertEquals("1 + 5",input.getExpression());
        Assert.assertEquals(false,input.isEncrypted());
        Assert.assertEquals(false,input.isRequired());
    }

    @org.springframework.context.annotation.Configuration
    public static class Configuration {

        @Bean
        public Yaml yaml(){
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser(){
            YamlParser yamlParser = new YamlParser();
            return yamlParser;
        }

        @Bean
        public Transformer inputTransformer(){
            return new InputsTransformer();
        }

    }
}