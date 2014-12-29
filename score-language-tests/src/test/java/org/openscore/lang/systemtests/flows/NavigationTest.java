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
import org.openscore.lang.systemtests.PathData;
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
        URI operationsPython = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operationsPython);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("userNumber", 12);
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, PathData> tasks = triggerWithData(compilationArtifact, userInputs);

        Assert.assertEquals(5, tasks.size());
        Assert.assertEquals("check_number", tasks.get("0/1").getName());
        Assert.assertEquals("process_even_number", tasks.get("0/2").getName());
    }

    @Test
    public void testComplexNavigationOddNumber() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_complex_navigation.yaml").toURI();
        URI operationsPython = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operationsPython);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("userNumber", 13);
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, PathData> tasks = triggerWithData(compilationArtifact, userInputs);

        Assert.assertEquals(5, tasks.size());
        Assert.assertEquals("check_number", tasks.get("0/1").getName());
        Assert.assertEquals("process_odd_number", tasks.get("0/2").getName());
    }

    @Test
    public void testComplexNavigationFailure() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_complex_navigation.yaml").toURI();
        URI operationsPython = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operationsPython);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("userNumber", 1024);
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, PathData> tasks = triggerWithData(compilationArtifact, userInputs);

        Assert.assertEquals(5, tasks.size());
        Assert.assertEquals("check_number", tasks.get("0/1").getName());
        Assert.assertEquals("send_error_mail", tasks.get("0/2").getName());
    }

    @Test
    public void testDefaultSuccessNavigation() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_default_navigation.yaml").toURI();
        URI operationsPython = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operationsPython);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("navigationType", "success");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, PathData> tasks = triggerWithData(compilationArtifact, userInputs);

        Assert.assertEquals(5, tasks.size());
        Assert.assertEquals("produce_default_navigation", tasks.get("0/1").getName());
        Assert.assertEquals("check_Weather", tasks.get("0/2").getName());
    }

    @Test
    public void testDefaultOnFailureNavigation() throws Exception {

        URI resource = getClass().getResource("/yaml/flow_default_navigation.yaml").toURI();
        URI operationsPython = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operationsPython);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("navigationType", "failure");
        userInputs.put("emailHost", "emailHost");
        userInputs.put("emailPort", "25");
        userInputs.put("emailSender", "user@host.com");
        userInputs.put("emailRecipient", "user@host.com");

        Map<String, PathData> tasks = triggerWithData(compilationArtifact, userInputs);

        Assert.assertEquals(5, tasks.size());
        Assert.assertEquals("produce_default_navigation", tasks.get("0/1").getName());
        Assert.assertEquals("send_error_mail", tasks.get("0/2").getName());
    }
}