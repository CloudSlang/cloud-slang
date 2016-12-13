/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import configuration.SlangEntitiesSpringConfig;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.spi.encryption.Encryption;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Maps;
import org.python.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 10/07/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RunEnvironmentSensitiveTest.RunEnvironmentSensitiveValueTestConfig.class,
        SlangEntitiesSpringConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RunEnvironmentSensitiveTest {
    private static final String ENCYPTED = "{Encrypted}";

    @Test
    public void testEmptyRunEnvironmentNotSensitive() {
        //everything is empty
        Set<SystemProperty> sp = Sets.newHashSet();
        RunEnvironment runEnvironment = new RunEnvironment(sp);
        assertFalse(runEnvironment.containsSensitiveData());

        Map<String, Value> callArguments = Maps.newHashMap();
        runEnvironment.putCallArguments(callArguments);
        assertFalse(runEnvironment.containsSensitiveData());

        runEnvironment.putReturnValues(null);
        assertFalse(runEnvironment.containsSensitiveData());

        Map<String, Value> outputs = Maps.newHashMap();
        ReturnValues returnValues = new ReturnValues(outputs, "result");
        runEnvironment.putReturnValues(returnValues);
        assertFalse(runEnvironment.containsSensitiveData());
    }

    @Test
    public void testRunEnvironmentOnlySpSensitive() {
        //everything is empty
        Set<SystemProperty> sp = Sets.newHashSet();
        sp.add(new SystemProperty("a.b", "key", ValueFactory.createEncryptedString("value")));

        RunEnvironment runEnvironment = new RunEnvironment(sp);
        assertTrue(runEnvironment.containsSensitiveData());

        Map<String, Value> callArguments = Maps.newHashMap();
        runEnvironment.putCallArguments(callArguments);
        assertTrue(runEnvironment.containsSensitiveData());

        runEnvironment.putReturnValues(null);
        assertTrue(runEnvironment.containsSensitiveData());

        Map<String, Value> outputs = Maps.newHashMap();
        ReturnValues returnValues = new ReturnValues(outputs, "result");
        runEnvironment.putReturnValues(returnValues);
        assertTrue(runEnvironment.containsSensitiveData());
    }

    @Test
    public void testRunEnvironmentOnlyCallArgsSensitive() {
        //everything is empty
        Set<SystemProperty> sp = Sets.newHashSet();
        sp.add(new SystemProperty("a.b", "key", "value"));

        RunEnvironment runEnvironment = new RunEnvironment(sp);
        assertFalse(runEnvironment.containsSensitiveData());

        Map<String, Value> callArguments = Maps.newHashMap();
        callArguments.put("arg", ValueFactory.create("val", true));

        runEnvironment.putCallArguments(callArguments);
        assertTrue(runEnvironment.containsSensitiveData());

        runEnvironment.putReturnValues(null);
        assertTrue(runEnvironment.containsSensitiveData());

        Map<String, Value> outputs = Maps.newHashMap();
        ReturnValues returnValues = new ReturnValues(outputs, "result");
        runEnvironment.putReturnValues(returnValues);
        assertTrue(runEnvironment.containsSensitiveData());
    }

    @Test
    public void testRunEnvironmentOnlyReturnValueSensitive() {
        //everything is empty
        Set<SystemProperty> sp = Sets.newHashSet();
        sp.add(new SystemProperty("a.b", "key", "value"));

        RunEnvironment runEnvironment = new RunEnvironment(sp);
        assertFalse(runEnvironment.containsSensitiveData());

        Map<String, Value> callArguments = Maps.newHashMap();
        callArguments.put("arg", ValueFactory.create("val", false));

        runEnvironment.putCallArguments(callArguments);
        assertFalse(runEnvironment.containsSensitiveData());

        Map<String, Value> outputs = Maps.newHashMap();
        outputs.put("output", ValueFactory.create("value", true));

        ReturnValues returnValues = new ReturnValues(outputs, "result");
        runEnvironment.putReturnValues(returnValues);
        assertTrue(runEnvironment.containsSensitiveData());
    }

    @Test
    public void testRunEnvironmentAllSensitive() {
        //everything is empty
        Set<SystemProperty> sp = Sets.newHashSet();
        SystemProperty systemProperty1 = new SystemProperty("a.b", "sp1",
                ValueFactory.createEncryptedString("systemProperty1"));
        sp.add(systemProperty1);
        SystemProperty systemProperty2 = new SystemProperty("a.b", "sp2",
                ValueFactory.createEncryptedString("systemProperty2"));
        sp.add(systemProperty2);

        RunEnvironment runEnvironment = new RunEnvironment(sp);
        assertTrue(runEnvironment.containsSensitiveData());

        Map<String, Value> callArguments = Maps.newHashMap();
        Value callValue1 = ValueFactory.create("callValue1", true);
        callArguments.put("callValue1", callValue1);
        Value callValue2 = ValueFactory.create("callValue2", true);
        callArguments.put("callValue2", callValue2);

        runEnvironment.putCallArguments(callArguments);
        assertTrue(runEnvironment.containsSensitiveData());

        Map<String, Value> outputs = Maps.newHashMap();
        Value output1 = ValueFactory.create("output1", true);
        outputs.put("output1", output1);
        Value output2 = ValueFactory.create("output2", true);
        outputs.put("output2", output2);

        ReturnValues returnValues = new ReturnValues(outputs, "result");
        runEnvironment.putReturnValues(returnValues);
        assertTrue(runEnvironment.containsSensitiveData());
        testEncrypted(systemProperty1, systemProperty2, callValue1, callValue2, output1, output2, true);

        runEnvironment.encryptSensitiveData();
        assertTrue(runEnvironment.containsSensitiveData());
        testEncrypted(systemProperty1, systemProperty2, callValue1, callValue2, output1, output2, true);

        runEnvironment.decryptSensitiveData();
        assertTrue(runEnvironment.containsSensitiveData());
        testEncrypted(systemProperty1, systemProperty2, callValue1, callValue2, output1, output2, false);

        runEnvironment.decryptSensitiveData();
        assertTrue(runEnvironment.containsSensitiveData());
        testEncrypted(systemProperty1, systemProperty2, callValue1, callValue2, output1, output2, false);

        runEnvironment.encryptSensitiveData();
        assertTrue(runEnvironment.containsSensitiveData());
        testEncrypted(systemProperty1, systemProperty2, callValue1, callValue2, output1, output2, true);
    }

    private void testEncrypted(SystemProperty systemProperty1, SystemProperty systemProperty2,
                               Value callValue1, Value callValue2,
                               Value output1, Value output2, boolean encrypted) {
        final String systemPropertyContent1 = ((SensitiveValue) systemProperty1.getValue()).getContent();
        final String systemPropertyContent2 = ((SensitiveValue) systemProperty2.getValue()).getContent();

        String sp1 = systemProperty1.getValue().get().toString();
        assertEquals("systemProperty1", sp1);
        String sp2 = systemProperty2.getValue().get().toString();
        assertEquals("systemProperty2", sp2);

        // Sensitive system property values are encrypted directly (without Base64 encoding)
        // since they are simple strings
        assertEquals(encrypted ? "{Encrypted}" + sp1 : sp1, systemPropertyContent1);
        assertEquals(encrypted ? "{Encrypted}" + sp2 : sp2, systemPropertyContent2);

        final String callValue1Content = ((SensitiveValue) callValue1).getContent();
        final String callValue2Content = ((SensitiveValue) callValue2).getContent();

        String ca1 = callValue1.get().toString();
        assertEquals("callValue1", ca1);
        String ca2 = callValue2.get().toString();
        assertEquals("callValue2", ca2);

        assertEquals(encrypted ? "{Encrypted}rO0ABXQACmNhbGxWYWx1ZTE=" : ca1, callValue1Content);
        assertEquals(encrypted ? "{Encrypted}rO0ABXQACmNhbGxWYWx1ZTI=" : ca2, callValue2Content);

        final String output1Content = ((SensitiveValue) output1).getContent();
        final String output2Content = ((SensitiveValue) output2).getContent();

        String o1 = output1.get().toString();
        assertEquals("output1", o1);
        String o2 = output2.get().toString();
        assertEquals("output2", o2);

        assertEquals(encrypted ? "{Encrypted}rO0ABXQAB291dHB1dDE=" : o1, output1Content);
        assertEquals(encrypted ? "{Encrypted}rO0ABXQAB291dHB1dDI=" : o2, output2Content);
    }

    @Configuration
    @ComponentScan("io.cloudslang.lang.entities.utils")
    static class RunEnvironmentSensitiveValueTestConfig {
        @Bean
        public Encryption getTestEncryption() {
            return new Encryption() {

                @Override
                public String encrypt(char[] clearText) {
                    return ENCYPTED + new String(clearText);
                }

                @Override
                public char[] decrypt(String cypherText) {
                    return cypherText.substring(ENCYPTED.length()).toCharArray();
                }

                @Override
                public boolean isTextEncrypted(String text) {
                    return text.startsWith(ENCYPTED);
                }
            };
        }
    }
}
