/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.runtime.events;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import static org.openscore.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;
import static org.openscore.lang.runtime.events.LanguageEventData.BOUND_INPUTS;
import static org.openscore.lang.runtime.events.LanguageEventData.DESCRIPTION;
import static org.openscore.lang.runtime.events.LanguageEventData.EXCEPTION;
import static org.openscore.lang.runtime.events.LanguageEventData.EXECUTIONID;
import static org.openscore.lang.runtime.events.LanguageEventData.OUTPUTS;
import static org.openscore.lang.runtime.events.LanguageEventData.PATH;
import static org.openscore.lang.runtime.events.LanguageEventData.TIMESTAMP;
import static org.openscore.lang.runtime.events.LanguageEventData.TYPE;
import static org.junit.Assert.assertEquals;

/**
 * @author moradi
 * @since 16/11/2014
 * @version $Id$
 */
public class LanguageEventDataTest {

	LanguageEventData eventData = new LanguageEventData();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getEventType()}.
	 */
	@Test
	public void testEventType() {
		String eventType = SLANG_EXECUTION_EXCEPTION;
		eventData.setEventType(eventType);
		assertEquals(eventType, eventData.getEventType());
		assertEquals(eventType, eventData.get(TYPE));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getDescription()}.
	 */
	@Test
	public void testDescription() {
		String desc = "My description";
		eventData.setDescription(desc);
		assertEquals(desc, eventData.getDescription());
		assertEquals(desc, eventData.get(DESCRIPTION));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getTimeStamp()}.
	 */
	@Test
	public void testTimeStamp() {
		Date ts = new Date();
		eventData.setTimeStamp(ts);
		assertEquals(ts, eventData.getTimeStamp());
		assertEquals(ts, eventData.get(TIMESTAMP));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getExecutionId()}.
	 */
	@Test
	public void testExecutionId() {
		Long exeId = 123L;
		eventData.setExecutionId(exeId);
		assertEquals(exeId, eventData.getExecutionId());
		assertEquals(exeId, eventData.get(EXECUTIONID));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getPath()}.
	 */
	@Test
	public void testPath() {
		String exePath = "0/1/2";
		eventData.setPath(exePath);
		assertEquals(exePath, eventData.getPath());
		assertEquals(exePath, eventData.get(PATH));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getException()}.
	 */
	@Test
	public void testException() {
		Exception ex = new Exception("My exception");
		eventData.setException(ex);
		assertEquals(ex, eventData.getException());
		assertEquals(ex, eventData.get(EXCEPTION));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getInputs()}.
	 */
	@Test
	public void testInputs() {
		HashMap<String, Serializable> inputs = new HashMap<String, Serializable>() {

			private static final long serialVersionUID = 161841000262993977L;
			{
				put("input1", "str1");
				put("input2", 123L);
				put("input3", true);
			}
		};
		eventData.setInputs(inputs);
		assertEquals(inputs, eventData.getInputs());
		assertEquals(inputs, eventData.get(BOUND_INPUTS));
	}

	/**
	 * Test method for {@link org.openscore.lang.runtime.events.LanguageEventData#getOutputs()}.
	 */
	@Test
	public void testOutputs() {
		HashMap<String, Serializable> outputs = new HashMap<String, Serializable>() {

			private static final long serialVersionUID = 161841000262993977L;
			{
				put("output1", "str1");
				put("output2", 123L);
				put("output3", false);
			}
		};
		eventData.setOutputs(outputs);
		assertEquals(outputs, eventData.getOutputs());
		assertEquals(outputs, eventData.get(OUTPUTS));
	}

}
