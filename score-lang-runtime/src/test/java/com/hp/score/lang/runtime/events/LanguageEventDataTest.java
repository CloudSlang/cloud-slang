/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.events;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_STEP_ERROR;
import static com.hp.score.lang.runtime.events.LanguageEventData.DESCRIPTION;
import static com.hp.score.lang.runtime.events.LanguageEventData.EXCEPTION;
import static com.hp.score.lang.runtime.events.LanguageEventData.EXECUTIONID;
import static com.hp.score.lang.runtime.events.LanguageEventData.INPUTS;
import static com.hp.score.lang.runtime.events.LanguageEventData.OUTPUTS;
import static com.hp.score.lang.runtime.events.LanguageEventData.PATH;
import static com.hp.score.lang.runtime.events.LanguageEventData.TIMESTAMP;
import static com.hp.score.lang.runtime.events.LanguageEventData.TYPE;

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
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getEventType()}.
	 */
	@Test
	public void testEventType() {
		String eventType = EVENT_STEP_ERROR;
		eventData.setEventType(eventType);
		assertEquals(eventType, eventData.getEventType());
		assertEquals(eventType, eventData.get(TYPE));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getDescription()}.
	 */
	@Test
	public void testDescription() {
		String desc = "My description";
		eventData.setDescription(desc);
		assertEquals(desc, eventData.getDescription());
		assertEquals(desc, eventData.get(DESCRIPTION));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getTimeStamp()}.
	 */
	@Test
	public void testTimeStamp() {
		Date ts = new Date();
		eventData.setTimeStamp(ts);
		assertEquals(ts, eventData.getTimeStamp());
		assertEquals(ts, eventData.get(TIMESTAMP));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getExecutionId()}.
	 */
	@Test
	public void testExecutionId() {
		Long exeId = 123L;
		eventData.setExecutionId(exeId);
		assertEquals(exeId, eventData.getExecutionId());
		assertEquals(exeId, eventData.get(EXECUTIONID));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getPath()}.
	 */
	@Test
	public void testPath() {
		String exePath = "0/1/2";
		eventData.setPath(exePath);
		assertEquals(exePath, eventData.getPath());
		assertEquals(exePath, eventData.get(PATH));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getException()}.
	 */
	@Test
	public void testException() {
		Exception ex = new Exception("My exception");
		eventData.setException(ex);
		assertEquals(ex, eventData.getException());
		assertEquals(ex, eventData.get(EXCEPTION));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getInputs()}.
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
		assertEquals(inputs, eventData.get(INPUTS));
	}

	/**
	 * Test method for {@link com.hp.score.lang.runtime.events.LanguageEventData#getOutputs()}.
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
