/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.lang.systemtests;

import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.dependency.impl.services.utils.UnzipUtil;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.score.events.ScoreEvent;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/*
 * Created by orius123 on 12/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/systemTestContext.xml")
public abstract class SystemsTestsParent {
    protected static final String EXEC_START_PATH = "0";
    protected static final String FIRST_STEP_PATH = "0.0";
    protected static final String SECOND_STEP_KEY = "0.1";
    protected static final String THIRD_STEP_KEY = "0.2";
    protected static final String FOURTH_STEP_KEY = "0.3";

    protected static final String BRANCH_FIRST_STEP_PATH = "0.0.0";
    protected static final String BRANCH_SECOND_STEP_KEY = "0.0.1";
    protected static final String BRANCH_THIRD_STEP_KEY = "0.0.2";
    protected static final String BRANCH_FOURTH_STEP_KEY = "0.0.3";

    static {
        ClassLoader classLoader = SystemsTestsParent.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        System.out.println("setting.xml path is [" + settingsXmlPath + "]");
        File rootHome = new File(settingsXmlPath).getParentFile();

        System.setProperty("app.home", rootHome.getAbsolutePath());

        System.out.println("app.home path is [" + rootHome.getAbsolutePath() + "]");

        File mavenHome = new File(rootHome, "maven");
        File mavenRepo = new File(rootHome, "test-mvn-repo");
        mavenRepo.mkdirs();

        System.out.println("Maven home [" + mavenHome.getAbsolutePath() + "]");
        System.out.println("Maven repo [" + mavenRepo.getAbsolutePath() + "]");

        UnzipUtil.unzipToFolder(mavenHome.getAbsolutePath(), classLoader.getResourceAsStream("maven.zip"));

        System.setProperty(MavenConfigImpl.MAVEN_HOME, mavenHome.getAbsolutePath());

        System.setProperty(MavenConfigImpl.MAVEN_REPO_LOCAL, mavenRepo.getAbsolutePath());
        System.setProperty("maven.home", classLoader.getResource("maven").getPath());

        String localRepository = System.getProperty(MavenConfigImpl.MAVEN_REPO_LOCAL);
        if (StringUtils.isNotEmpty(localRepository)) {
            System.setProperty("maven.repo.local", localRepository);
        }

        System.setProperty(MavenConfigImpl.MAVEN_SETTINGS_PATH, settingsXmlPath);
        String m2_conf_path = classLoader.getResource("m2.conf").getPath();
        System.out.println("m2.conf path [" + m2_conf_path + "]");
        System.setProperty(MavenConfigImpl.MAVEN_M2_CONF_PATH, m2_conf_path);

        String provideralAlreadyConfigured = System.setProperty("python.executor.engine", PythonExecutionCachedEngine.class.getSimpleName());
        assertNull("python.executor.engine was configured before this test!!!!!!!", provideralAlreadyConfigured);
    }

    @Autowired
    protected Slang slang;

    @Autowired
    protected SlangCompiler slangCompiler;

    @Autowired
    protected TriggerFlows triggerFlows;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected ScoreEvent trigger(CompilationArtifact compilationArtifact, Map<String, Value> userInputs, Set<SystemProperty> systemProperties) {
        return triggerFlows.runSync(compilationArtifact, userInputs, systemProperties);
    }

	public RuntimeInformation triggerWithData(CompilationArtifact compilationArtifact, Map<String, Value> userInputs, Set<SystemProperty> systemProperties) {
		return triggerFlows.runWithData(compilationArtifact, userInputs, systemProperties);
	}

    protected List<String> getStepsOnly(Map<String, StepData> stepsData) {
        return select(stepsData.keySet(), startsWith("0."));
    }

    protected Set<SystemProperty> loadSystemProperties(SlangSource source) {
        return slang.loadSystemProperties(source);
    }

    protected void verifyInOutParams(Map<String, Serializable> params) {
        if (params != null) {
            List<String> errorsInSensitivity = new ArrayList<>();
            for (Map.Entry<String, Serializable> entry : params.entrySet()) {
                String name = entry.getKey();
                boolean sensitive = entry.getValue() != null && entry.getValue().equals(SensitiveValue.SENSITIVE_VALUE_MASK);
                if (!(name.contains("sensitive") && sensitive || !name.contains("sensitive") && !sensitive)) {
                    errorsInSensitivity.add(name);
                }
                boolean success = true;
                String errorMessage = "\nSensitivity not set properly for: " + Arrays.toString(errorsInSensitivity.toArray(new String[errorsInSensitivity.size()]));
                if (errorsInSensitivity.size() > 0) {
                    System.out.println(errorMessage);
                    success = false;
                }
                assertTrue(errorMessage, success);
            }
        }
    }
}
