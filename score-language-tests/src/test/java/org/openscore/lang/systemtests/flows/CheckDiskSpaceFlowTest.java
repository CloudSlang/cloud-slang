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
import org.junit.Ignore;
import org.junit.Test;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.systemtests.StepData;
import org.openscore.lang.systemtests.SystemsTestsParent;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Date: 1/7/2015
 *
 * @author lesant
 */

public class CheckDiskSpaceFlowTest extends SystemsTestsParent {

    @Test
    @Ignore
    public void testCompileAndRunFlow() throws Exception {

        URI resource = getClass().getResource("/yaml/docker-demo/diskspace_health_check_flow.yaml").toURI();
        URI dockerOperations = getClass().getResource("/yaml/docker-demo/docker_operations.yaml").toURI();
        URI clearDockerImagesFlow = getClass().getResource("/yaml/docker-demo/clear_docker_images_flow.yaml").toURI();
        URI getUsedImagesFlow = getClass().getResource("/yaml/docker-demo/get_used_images_flow.yaml").toURI();
        URI linuxOperations = getClass().getResource("/yaml/docker-demo/linux_operations.yaml").toURI();
        URI subtractOperation = getClass().getResource("/yaml/docker-demo/subtract_sets_op.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(clearDockerImagesFlow));
        path.add(SlangSource.fromFile(getUsedImagesFlow));
        path.add(SlangSource.fromFile(dockerOperations));
        path.add(SlangSource.fromFile(linuxOperations));
        path.add(SlangSource.fromFile(subtractOperation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("dockerHost", "{{ dockerHost }}");
        userInputs.put("dockerUsername", "{{ dockerUsername }}");
        userInputs.put("dockerPassword", "{{ dockerPassword }}");
        userInputs.put("percentage", "65%");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs);


    }

}

