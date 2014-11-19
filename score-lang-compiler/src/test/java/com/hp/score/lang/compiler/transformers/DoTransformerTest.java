package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.compiler.SlangTextualKeys;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DoTransformerTest {

    @Autowired
    private Transformer doTransformer;

    @Autowired
    private YamlParser yamlParser;

    private Object doInputsMap;

    @Before
    public void init() throws URISyntaxException {
        URL resource = getClass().getResource("/flow_with_data.yaml");
        SlangFile file = yamlParser.loadSlangFile(new File(resource.toURI()));
        Map flow = (Map)file.getFlow().get(SlangTextualKeys.WORKFLOW_KEY);
        Map task= (Map)flow.get("CheckWeather");
        doInputsMap = task.get(SlangTextualKeys.DO_KEY);
    }

    @Test
    public void testTransformExpression() throws Exception {
        List<Input> inputs = (List<Input>) doTransformer.transform(doInputsMap);
        Assert.assertFalse(inputs.isEmpty());
        Assert.assertEquals(2,inputs.size());
        Input input = inputs.iterator().next();
        Assert.assertEquals("city",input.getName());
        Assert.assertEquals("city_name",input.getExpression());
    }

    @Test
    public void testTransformConst() throws Exception {
        List<Input> inputs = (List<Input>) doTransformer.transform(doInputsMap);
        Assert.assertFalse(inputs.isEmpty());

        Input input = inputs.get(1);
        Assert.assertEquals("country",input.getName());
        Assert.assertEquals("str('Israel')",input.getExpression());
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
        public Transformer inputTransformer() {
            return new DoTransformer();
        }

    }
}