package com.hp.score.lang.tests.runtime.bindings;

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
import com.hp.score.lang.runtime.bindings.InputsBinding;
import com.hp.score.lang.runtime.bindings.ScriptEvaluator;
import com.hp.score.lang.runtime.configuration.SlangRuntimeSpringConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class InputsBindingTest {

    @Autowired
    private InputsBinding inputsBinding;

    @Test
    public void testEmptyBindInputs() throws Exception {
        List<Input> inputs = Lists.newArrayList();
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValue() throws Exception {
        List<Input> inputs = Lists.newArrayList(createDefaultValueInput("value"));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("value", result.get("input1"));
    }

    @Test
    public void testDefaultValueInt() throws Exception {
        List<Input> inputs = Lists.newArrayList(createDefaultValueInput(2));
        Map<String,Serializable> result = inputsBinding.bindInputs(new HashMap<String,Serializable>(),inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(2, result.get("input1"));
    }

    @Test
    public void testTwoInputs() throws Exception {
        List<Input> inputs = Lists.newArrayList(new Input("input2",null,"yyy",false,false),createDefaultValueInput("zzz"));
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
        List<Input> inputs = Lists.newArrayList(new Input("input1","inputX"));
        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("xxx", result.get("input1"));
    }

    @Test
    public void testInputScriptEval() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valX",5);
        Input scriptInput = new Input("input1","3 + valX");
        List<Input> inputs = Lists.newArrayList(scriptInput);
        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals(8, result.get("input1"));
    }

    @Test
    public void testInputScriptEval2() throws Exception {
        Map<String,Serializable> context = new HashMap<>();
        context.put("valB","b");
        context.put("valC","c");
        Input scriptInput = new Input("input1"," 'a' + valB + valC");
        List<Input> inputs = Lists.newArrayList(scriptInput);
        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("abc", result.get("input1"));
    }

    @Test
    public void testDefaultValueVsEmptyRef() throws Exception {
        Map<String,Serializable> context = new HashMap<>();

        Input refInput = new Input("input1","NotExistent","val",false,false);
        List<Input> inputs = Lists.newArrayList(refInput);

        Map<String,Serializable> result = inputsBinding.bindInputs(context,inputs);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("input1"));
        Assert.assertEquals("val", result.get("input1"));
    }

    private Input createDefaultValueInput(Serializable value){
        return new Input("input1",null,value,false,false);
    }

    @Configuration
    @Import(SlangRuntimeSpringConfig.class)
    static class Config{

        @Bean
        public InputsBinding inputsBinding(){
            return new InputsBinding();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator(){
            return new ScriptEvaluator();
        }
    }
}