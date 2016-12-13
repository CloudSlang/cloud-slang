/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.values;

import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.spi.encryption.Encryption;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ValueFactoryTest.Config.class)
public class ValueFactoryTest {

    @Test
    public void testCreatePyObjectValue() {
        PyObjectValue value = ValueFactory.createPyObjectValue("value1", false);
        Assert.assertEquals("value1", value.get());
    }

    @Test
    public void testCreatePyObjectValueFromValue() {
        Value value = ValueFactory.create("value1", false);
        PyObjectValue pyObjectValue = ValueFactory.createPyObjectValue(value);
        Assert.assertEquals("value1", pyObjectValue.get());
    }

    @Test
    public void testCreatePyObjectValueFromValueSensitive() {
        Value value = ValueFactory.create("value1", true);
        PyObjectValue pyObjectValue = ValueFactory.createPyObjectValue(value);
        Assert.assertEquals("value1", pyObjectValue.get());
    }

    static class Config {

        @Bean
        public Encryption getEncryption() {
            return new DummyEncryptor();
        }
    }
}
