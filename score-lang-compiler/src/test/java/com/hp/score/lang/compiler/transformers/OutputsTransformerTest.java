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
package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.model.SlangFile;
import com.hp.score.lang.compiler.utils.YamlParser;
import com.hp.score.lang.entities.bindings.Output;

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

/**
 * Date: 11/11/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OutputsTransformerTest.Configuration.class)
public class OutputsTransformerTest {

    private static final long DEFAULT_TIMEOUT = 10000;

    @Autowired
    private Transformer outputTransformer;

    @Autowired
    private YamlParser yamlParser;

    private Object outputsMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/operation_with_data.yaml");
        SlangFile file = yamlParser.loadSlangFile(new File(resource.toURI()));
        Map op = file.getOperations().iterator().next();
        Map<String, Object> opProp = (Map) op.get("test_op_2");
        outputsMap = opProp.get(SlangTextualKeys.OUTPUTS_KEY);
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testTransform() throws Exception {
        List<Output> outputs = (List<Output>) outputTransformer.transform(outputsMap);
        Assert.assertFalse(outputs.isEmpty());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testNoExpression() throws Exception {
        List<Output> outputs = (List<Output>) outputTransformer.transform(outputsMap);
        Output output = outputs.get(3);
        Assert.assertEquals("output4", output.getName());
        Assert.assertEquals("output4", output.getExpression());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testExpressionKeyFromActionReturnValues() throws Exception {
        List<Output> outputs = (List<Output>) outputTransformer.transform(outputsMap);
        Output output = outputs.get(0);
        Assert.assertEquals("output1", output.getName());
        Assert.assertEquals("input1", output.getExpression());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testInputExpression() throws Exception {
        List<Output> outputs = (List<Output>) outputTransformer.transform(outputsMap);
        Output output = outputs.get(2);
        Assert.assertEquals("output3", output.getName());
        Assert.assertEquals("fromInputs['input1']", output.getExpression());
    }

    @org.springframework.context.annotation.Configuration
    public static class Configuration {

        @Bean
        public Yaml yaml() {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml;
        }

        @Bean
        public YamlParser yamlParser() {
            YamlParser yamlParser = new YamlParser();
            return yamlParser;
        }

        @Bean
        public Transformer outputTransformer() {
            return new OutputsTransformer();
        }

    }
}
