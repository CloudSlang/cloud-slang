/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.configuration;

import io.cloudslang.lang.commons.configuration.SlangCommonsSpringConfig;
import io.cloudslang.lang.logging.LoggingServiceImpl;
import io.cloudslang.lang.tools.build.SlangBuilder;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.tester.parallel.report.LoggingSlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.SlangTestCaseRunReportGeneratorService;
import io.cloudslang.lang.tools.build.tester.parallel.services.ParallelTestCaseExecutorService;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestRunInfoServiceImpl;
import io.cloudslang.lang.tools.build.validation.StaticValidator;
import io.cloudslang.lang.tools.build.validation.StaticValidatorImpl;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SlangCommonsSpringConfig.class)
public class SlangBuildSpringConfiguration {

    @Bean
    public SlangBuilder slangBuild() {
        return new SlangBuilder();
    }

    @Bean
    public SlangContentVerifier slangContentVerifier() {
        return new SlangContentVerifier();
    }

    @Bean
    public StaticValidator staticValidator() {
        return new StaticValidatorImpl();
    }

    @Bean
    public SlangTestRunner slangTestRunner() {
        return new SlangTestRunner();
    }

    @Bean
    public TestCasesYamlParser parser() {
        return new TestCasesYamlParser();
    }

    @Bean
    public ParallelTestCaseExecutorService parallelTestCaseExecutorService() {
        return new ParallelTestCaseExecutorService();
    }

    @Bean
    public TestCaseEventDispatchService testCaseEventDispatchService() {
        return new TestCaseEventDispatchService();
    }

    @Bean
    public SlangTestCaseRunReportGeneratorService reportGeneratorService() {
        return new SlangTestCaseRunReportGeneratorService();
    }

    @Bean
    public TestRunInfoServiceImpl runConfigurationService() {
        return new TestRunInfoServiceImpl();
    }

    @Bean
    public LoggingServiceImpl loggingService() {
        return new LoggingServiceImpl();
    }

    @Bean
    public LoggingSlangTestCaseEventListener loggingSlangTestCaseEventListener() {
        return new LoggingSlangTestCaseEventListener();
    }

}
