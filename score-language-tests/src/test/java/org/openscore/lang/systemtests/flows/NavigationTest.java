/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.systemtests.flows;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.systemtests.StepData;
import org.openscore.lang.systemtests.SystemsTestsParent;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Date: 12/4/2014
 *
 * @author Bonczidai Levente
 */
public class NavigationTest extends SystemsTestsParent {

    @Test
    public void testComplexNavigationEvenNumber() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_complex_navigation.yaml").toURI();
        URI operation1Python = getClass().getResource("/yaml/check_number.sl").toURI();
        URI operation2Python = getClass().getResource("/yaml/process_even_number.sl").toURI();
        URI operation3Python = getClass().getResource("/yaml/process_odd_number.sl").toURI();
        URI operation4Python = getClass().getResource("/yaml/send_email_mock.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1Python),
                SlangSource.fromFile(operation2Python),
                SlangSource.fromFile(operation3Python),
                SlangSource.fromFile(operation4Python));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("userNumber", "12");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals(5, tasks.size());
        Assert.assertEquals("check_number", tasks.get(FIRST_STEP_PATH).getName());
        Assert.assertEquals("process_even_number", tasks.get(SECOND_STEP_KEY).getName());
    }

    @Test
    public void testComplexNavigationOddNumber() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_complex_navigation.yaml").toURI();
        URI operation1Python = getClass().getResource("/yaml/check_number.sl").toURI();
        URI operation2Python = getClass().getResource("/yaml/process_even_number.sl").toURI();
        URI operation3Python = getClass().getResource("/yaml/process_odd_number.sl").toURI();
        URI operation4Python = getClass().getResource("/yaml/send_email_mock.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1Python),
                                                SlangSource.fromFile(operation2Python),
                                                SlangSource.fromFile(operation3Python),
                                                SlangSource.fromFile(operation4Python));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("userNumber", "13");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals("check_number", tasks.get(FIRST_STEP_PATH).getName());
        Assert.assertEquals("process_odd_number", tasks.get(SECOND_STEP_KEY).getName());
    }

    @Test
    public void testComplexNavigationFailure() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_complex_navigation.yaml").toURI();
        URI operation1Python = getClass().getResource("/yaml/check_number.sl").toURI();
        URI operation2Python = getClass().getResource("/yaml/process_even_number.sl").toURI();
        URI operation3Python = getClass().getResource("/yaml/process_odd_number.sl").toURI();
        URI operation4Python = getClass().getResource("/yaml/send_email_mock.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1Python),
                SlangSource.fromFile(operation2Python),
                SlangSource.fromFile(operation3Python),
                SlangSource.fromFile(operation4Python));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("userNumber", "1024");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals("check_number", tasks.get(FIRST_STEP_PATH).getName());
        Assert.assertEquals("send_error_mail", tasks.get(SECOND_STEP_KEY).getName());
    }

    @Test
    public void testDefaultSuccessNavigation() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_default_navigation.yaml").toURI();
        URI operationPython = getClass().getResource("/yaml/produce_default_navigation.sl").toURI();
        URI operation2Python = getClass().getResource("/yaml/send_email_mock.sl").toURI();
        URI operation3Python = getClass().getResource("/yaml/check_Weahther.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operationPython), SlangSource.fromFile(operation2Python), SlangSource.fromFile(operation3Python));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("navigationType", "success");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals("produce_default_navigation", tasks.get(FIRST_STEP_PATH).getName());
        Assert.assertEquals("check_Weather", tasks.get(SECOND_STEP_KEY).getName());
    }

    @Test
    public void testDefaultOnFailureNavigation() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_default_navigation.yaml").toURI();
        URI operationPython = getClass().getResource("/yaml/produce_default_navigation.sl").toURI();
        URI operation2Python = getClass().getResource("/yaml/send_email_mock.sl").toURI();
        URI operation3Python = getClass().getResource("/yaml/check_Weahther.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operationPython), SlangSource.fromFile(operation2Python), SlangSource.fromFile(operation3Python));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("navigationType", "failure");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals("produce_default_navigation", tasks.get(FIRST_STEP_PATH).getName());
        Assert.assertEquals("send_error_mail", tasks.get(SECOND_STEP_KEY).getName());
    }
}