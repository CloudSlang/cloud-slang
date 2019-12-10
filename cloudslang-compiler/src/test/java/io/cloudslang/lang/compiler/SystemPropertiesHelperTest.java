/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_SETTINGS_KEY;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.SystemPropertiesHelper;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SlangCompilerSpringConfig.class})
public class SystemPropertiesHelperTest {

    private ParsedSlang parsedSlangSysProps;
    private ParsedSlang parsedSlangNoSysProps;

    @Autowired
    private YamlParser yamlParserMock;

    @Autowired
    private SystemPropertiesHelper systemPropertiesHelper;

    @Before
    public void setUp() throws URISyntaxException {
        URI resourceSysProps = getClass().getResource("/seq-operation/seq_op_with_sys_property.sl").toURI();
        parsedSlangSysProps = yamlParserMock.parse(SlangSource.fromFile(new File(resourceSysProps)));
        URI resourceNoSysProps = getClass().getResource("/seq-operation/seq_op_no_sys_prop.sl").toURI();
        parsedSlangNoSysProps = yamlParserMock.parse(SlangSource.fromFile(new File(resourceNoSysProps)));
    }

    @Test
    public void testResultSetSystemPropertiesObjRepository() {
        Set<String> systemProperties = newHashSet("xpath", "title", "type");
        Map objRepository = parsedSlangSysProps.getObjectRepository();
        assertNotNull(objRepository);
        assertEquals(systemPropertiesHelper.getObjectRepositorySystemProperties(objRepository), systemProperties);
    }

    @Test
    public void testResultSetSystemPropertiesSettings() {
        Set<String> systemProperties = newHashSet("web_address", "sap_server", "std_window_path");
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) ((Map) parsedSlangSysProps.getOperation()
                .get(SlangTextualKeys.SEQ_ACTION_KEY))
                .get(SEQ_SETTINGS_KEY);
        assertNotNull(settings);
        assertEquals(systemPropertiesHelper.getSystemPropertiesFromSettings(settings), systemProperties);
    }

    @Test
    public void testResultSetEmptySystemProperties() {
        Map objRepository = parsedSlangNoSysProps.getObjectRepository();
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) ((Map) parsedSlangNoSysProps.getOperation()
                .get(SlangTextualKeys.SEQ_ACTION_KEY))
                .get(SEQ_SETTINGS_KEY);
        assertNotNull(objRepository);
        assertEquals(systemPropertiesHelper.getObjectRepositorySystemProperties(objRepository), newHashSet());
        assertEquals(systemPropertiesHelper.getSystemPropertiesFromSettings(settings), newHashSet());
    }

    static class Config {

        @Bean
        public SystemPropertiesHelper systemPropertiesHelper() {
            return mock(SystemPropertiesHelper.class);
        }
    }
}