package com.hp.score.lang.runtime.bindings;

/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

import com.hp.score.lang.entities.bindings.Input;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = InputsBindingTest.Config.class)
public class InputsBindingTest {

    @Autowired
    private InputsBinding inputsBinding;

    @Test
    public void testEmptyBindInputs() throws Exception {
        List<Input> inputs = Arrays.asList();
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValue() throws Exception {
        List<Input> inputs = Arrays.asList(createDefaultValueInput("value"));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("value", result.get("input1"));
    }

    @Test
    public void testDefaultValueInt() throws Exception {
        List<Input> inputs = Arrays.asList(createDefaultValueInput(2));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(2, result.get("input1"));
    }

    @Test
    public void testTwoInputs() throws Exception {
        List<Input> inputs = Arrays.asList(new Input("input2",null,"yyy",false,false,false),createDefaultValueInput("zzz"));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("zzz", result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals("yyy", result.get("input2"));
    }

    @Test
    public void testInputRef() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("inputX","xxx");
        List<Input> inputs = Arrays.asList(new Input("input1","inputX"));
        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("xxx", result.get("input1"));

        Assert.assertEquals(1,context.size());
    }

    @Test
    public void testInputScriptEval() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valX",5);
        Input scriptInput = new Input("input1","3 + valX");
        List<Input> inputs = Arrays.asList(scriptInput);
        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(8, result.get("input1"));

        Assert.assertEquals(1,context.size());
    }

    @Test
    public void testInputScriptEval2() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valB","b");
        context.put("valC","c");
        Input scriptInput = new Input("input1"," 'a' + valB + valC");
        List<Input> inputs = Arrays.asList(scriptInput);
        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("abc", result.get("input1"));
    }

    @Test
    public void testDefaultValueVsEmptyRef() throws Exception {
        Map<String,Serializable> context = new HashMap<>();

        Input refInput = new Input("input1","NotExistent","val",false,false,false);
        List<Input> inputs = Arrays.asList(refInput);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("val", result.get("input1"));

        Assert.assertTrue(context.isEmpty());
    }

    @Test
    public void testAssignFromAndExpr() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
        Input input = new Input("input1","5+7",null,false,false,false);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));

        Assert.assertEquals(1,context.size());
        Assert.assertEquals(3,context.get("input1"));
    }

    @Test
    public void testAssignFromAndConst() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
        Input input = new Input("input1",null,5,false,false,false);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
    }

    @Test
    public void testComplexExpr() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",3);
        Input input = new Input("input2"," input1 + 3 * 2 ",null,false,false,false);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(9, result.get("input2"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testAssignFromVsRef() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input2",3);
        context.put("input1",5);
        Input input = new Input("input1","input2",null,false,false,false);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFrom() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input2",3);
        context.put("input1",5);
        Input input = new Input("input1","input2",null,false,false,true);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
        Assert.assertEquals(1, result.size());

        Assert.assertEquals(2, context.size());
    }

    @Test
    public void testOverrideAssignFrom2() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",5);
        Input input = new Input("input1",null,3,false,false,true);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(3, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFrom3() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",5);
        Input input = new Input("input1",null,null,false,false,true);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("override disables the assignFrom func...",null, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideFalse() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("input1",5);
        Input input = new Input("input1",null,6,false,false,false);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testExpressionWithWrongRef() throws Exception {
        Map<String,Serializable> context = new HashMap<>();

        Input input = new Input("input1","input2",null,false,false,false);
        List<Input> inputs = Arrays.asList(input);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(null, result.get("input1"));
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testInputAssignFromAnotherInput() throws Exception {
        Map<String,Serializable> context = new HashMap<>();

        Input input1 = new Input("input1",null,5,false,false,false);
        Input input2 = new Input("input2","input1");
        List<Input> inputs = Arrays.asList(input1,input2);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(5, result.get("input2"));
        Assert.assertEquals(2, result.size());

        Assert.assertTrue("orig context should not change",context.isEmpty());
    }

    @Test
    public void testComplexExpressionInput() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("varX",5);

        Input input1 = new Input("input1",null,5,false,false,false);
        Input input2 = new Input("input2","input1 + 5 + varX");
        List<Input> inputs = Arrays.asList(input1,input2);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(5, result.get("input1"));
        Assert.assertTrue(result.containsKey("input2"));
        Assert.assertEquals(15, result.get("input2"));
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }

    @Test
    public void testComplexExpression2Input() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("varX","roles");

        Input input1 = new Input("input1","\"mighty\" + ' max '   + varX",null,false,false,false);
        List<Input> inputs = Arrays.asList(input1);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("mighty max roles", result.get("input1"));
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("orig context should not change",1,context.size());
    }


    private Input createDefaultValueInput(Serializable value){
        return new Input("input1",null,value,false,false,false);
    }

    @Configuration
    static class Config{

        @Bean
        public InputsBinding inputsBinding(){
            return new InputsBinding();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator(){
            return new ScriptEvaluator();
        }


        @Bean
        public ScriptEngine scriptEngine(){
            return  new ScriptEngineManager().getEngineByName("python");
        }
    }
}