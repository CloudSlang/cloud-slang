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
import io.cloudslang.lang.entities.SlangSystemPropertyConstant;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.score.events.ScoreEvent;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNull;

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
        File rootHome = new File(settingsXmlPath).getParentFile();
        File mavenHome = new File(rootHome, "maven");
        File mavenRepo = new File(rootHome, "test-mvn-repo");
        mavenRepo.mkdirs();

        UnzipUtil.unzipToFolder(mavenHome.getAbsolutePath(), classLoader.getResourceAsStream("maven.zip"));

        System.setProperty(MavenConfigImpl.MAVEN_HOME, mavenHome.getAbsolutePath());

        System.setProperty(MavenConfigImpl.MAVEN_REPO_LOCAL, mavenRepo.getAbsolutePath());
        System.setProperty(MavenConfigImpl.MAVEN_REMOTE_URL, "http://mydtbld0034.hpeswlab.net:8081/nexus/content/groups/oo-public");
        System.setProperty(MavenConfigImpl.MAVEN_PLUGINS_URL, "http://mydphdb0166.hpswlabs.adapps.hp.com:8081/nexus/content/repositories/snapshots/");
        System.setProperty("maven.home", classLoader.getResource("maven").getPath());

        System.setProperty(MavenConfigImpl.MAVEN_PROXY_PROTOCOL, "https");
        System.setProperty(MavenConfigImpl.MAVEN_PROXY_HOST, "proxy.bbn.hp.com");
        System.setProperty(MavenConfigImpl.MAVEN_PROXY_PORT, "8080");
        System.setProperty(MavenConfigImpl.MAVEN_PROXY_NON_PROXY_HOSTS, "*.hp.com");

        System.setProperty(MavenConfigImpl.MAVEN_SETTINGS_PATH, settingsXmlPath);
        System.setProperty(MavenConfigImpl.MAVEN_M2_CONF_PATH, classLoader.getResource("m2.conf").getPath());

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

    static {
        System.setProperty(SlangSystemPropertyConstant.CSLANG_ENCODING.getValue(), "utf-8");
    }

    protected ScoreEvent trigger(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> userInputs, Set<SystemProperty> systemProperties) {
        return triggerFlows.runSync(compilationArtifact, userInputs, systemProperties);
    }

	public RuntimeInformation triggerWithData(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> userInputs, Set<SystemProperty> systemProperties) {
		return triggerFlows.runWithData(compilationArtifact, userInputs, systemProperties);
	}

    protected List<String> getStepsOnly(Map<String, StepData> stepsData) {
        return select(stepsData.keySet(), startsWith("0."));
    }

    protected Set<SystemProperty> loadSystemProperties(SlangSource source) {
        return slang.loadSystemProperties(source);
    }

}
