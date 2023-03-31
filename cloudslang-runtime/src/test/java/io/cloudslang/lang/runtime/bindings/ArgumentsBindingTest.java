/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.dependency.impl.services.utils.UnzipUtil;
import io.cloudslang.lang.entities.PromptType;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.services.ScriptsService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorCommunicationService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.executor.services.PythonExecutorLifecycleManagerService;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.api.python.executor.entities.PythonExecutorDetails;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionNotCachedEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.executor.services.ExternalPythonExecutorServiceImpl;
import io.cloudslang.runtime.impl.python.executor.services.PythonExecutorLifecycleManagerServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ArgumentsBindingTest.Config.class)
public class ArgumentsBindingTest {
    private static boolean shouldRunMaven;

    static {
        ClassLoader classLoader = ArgumentsBindingTest.class.getClassLoader();

        String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();
        File mavenHome = new File(rootHome, "maven");
        File mavenRepo = new File(rootHome, "test-mvn-repo");
        if (!mavenRepo.exists() && !mavenRepo.mkdirs()) {
            System.out.println("Could not create maven repo " + mavenRepo.toString());
        }

        UnzipUtil.unzipToFolder(mavenHome.getAbsolutePath(), classLoader.getResourceAsStream("maven.zip"));

        System.setProperty(MavenConfigImpl.MAVEN_HOME, mavenHome.getAbsolutePath());

        System.setProperty(MavenConfigImpl.MAVEN_REPO_LOCAL, mavenRepo.getAbsolutePath());
        System.setProperty("maven.home", classLoader.getResource("maven.zip").getPath());

        shouldRunMaven = System.getProperties().containsKey(MavenConfigImpl.MAVEN_REMOTE_URL) &&
                System.getProperties().containsKey(MavenConfigImpl.MAVEN_PLUGINS_URL);

        System.setProperty(MavenConfigImpl.MAVEN_SETTINGS_PATH, settingsXmlPath);
        System.setProperty(MavenConfigImpl.MAVEN_M2_CONF_PATH, classLoader.getResource("m2.conf").getPath());

        String provideralAlreadyConfigured = System.setProperty("python.executor.engine",
                PythonExecutionNotCachedEngine.class.getSimpleName());
        assertNull("python.executor.engine was configured before this test!!!!!!!", provideralAlreadyConfigured);

        System.setProperty("python.expressionsEval", "jython");
    }

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Autowired
    private ArgumentsBinding argumentsBinding;

    @Test
    public void testEmptyBindArguments() throws Exception {
        List<Argument> arguments = Collections.emptyList();
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDefaultValueNoExpression() {
        List<Argument> arguments = Collections.singletonList(new Argument("argument1", ValueFactory.create("value")));
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("value", result.get("argument1").get());
    }

    @Test
    public void testDefaultValueExpression() {
        List<Argument> arguments = Collections.singletonList(new Argument("argument1",
                ValueFactory.create("${ 'value' }")));
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("value", result.get("argument1").get());
    }

    @Ignore("Remove when types are supported")
    @Test
    public void testDefaultValueInt() {
        List<Argument> arguments = Collections.singletonList(new Argument("argument1", ValueFactory.create(2)));
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(2, result.get("argument1").get());
    }

    @Ignore("Remove when types are supported")
    @Test
    public void testDefaultValueBoolean() {
        List<Argument> arguments = Arrays.asList(
                new Argument("argument1", ValueFactory.create(true)),
                new Argument("argument2", ValueFactory.create(false)),
                new Argument("argument3", ValueFactory.create("phrase containing true and false"))
        );
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertTrue((boolean) result.get("argument1").get());
        Assert.assertFalse((boolean) result.get("argument2").get());
        Assert.assertEquals("phrase containing true and false", result.get("argument3").get());
    }

    @Test
    public void testTwoArguments() {
        List<Argument> arguments = Arrays.asList(new Argument("argument2", ValueFactory.create("yyy")),
                new Argument("argument1", ValueFactory.create("zzz")));
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("zzz", result.get("argument1").get());
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals("yyy", result.get("argument2").get());
    }

    @Test
    public void testAssignNoExpression() {
        Argument argument1 = new Argument("argument1", ValueFactory.create("${ argument1 }"));
        Argument argument2 = new Argument("argument2", ValueFactory.create("${ argument1 }"));
        List<Argument> arguments = Arrays.asList(argument1, argument2);
        Map<String, Value> result = bindArguments(arguments);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals(null, result.get("argument1").get());
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals(null, result.get("argument2").get());
    }

    @Test
    public void testArgumentRef() {
        Map<String, Value> context = new HashMap<>();
        context.put("argumentX", ValueFactory.create("xxx"));
        List<Argument> arguments = Collections.singletonList(new Argument("argument1",
                ValueFactory.create("${ str(argumentX) }")));
        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("xxx", result.get("argument1").get());

        Assert.assertEquals(1, context.size());
    }

    @Test
    public void testArgumentScriptEval() {
        Map<String, Value> context = new HashMap<>();
        context.put("valX", ValueFactory.create("5"));
        Argument scriptArgument = new Argument("argument1", ValueFactory.create("${ \"3\" + valX }"));
        List<Argument> arguments = Collections.singletonList(scriptArgument);
        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("35", result.get("argument1").get());

        Assert.assertEquals(1, context.size());
    }

    @Test
    public void testArgumentScriptEval2() {
        Map<String, Value> context = new HashMap<>();
        context.put("valB", ValueFactory.create("b"));
        context.put("valC", ValueFactory.create("c"));
        Argument scriptArgument = new Argument("argument1", ValueFactory.create("${ 'a' + valB + valC }"));
        List<Argument> arguments = Collections.singletonList(scriptArgument);
        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("abc", result.get("argument1").get());
    }

    @Test
    public void testDefaultValueVsEmptyRef() {
        Map<String, Value> context = new HashMap<>();

        Argument refArgument = new Argument("argument1", ValueFactory.create("${ str('val') }"));
        List<Argument> arguments = Collections.singletonList(refArgument);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("val", result.get("argument1").get());

        Assert.assertTrue(context.isEmpty());
    }

    @Test
    public void testOverridableFalseBehaviour() {
        Map<String, Value> context = new HashMap<>();
        context.put("argument1", ValueFactory.create("3"));
        Argument argument = new Argument("argument1", ValueFactory.create("${ \"5+7\" }"));
        List<Argument> arguments = Collections.singletonList(argument);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("5+7", result.get("argument1").get());

        Assert.assertEquals(1, context.size());
        Assert.assertEquals("3", context.get("argument1").get());
    }

    @Test
    public void testComplexExpr() {
        Map<String, Value> context = new HashMap<>();
        context.put("argument1", ValueFactory.create("3"));
        Argument argument = new Argument("argument2", ValueFactory.create("${ argument1 + \"3 * 2\" }"));
        List<Argument> arguments = Collections.singletonList(argument);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals("33 * 2", result.get("argument2").get());
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testOverrideAssignFromVar() {
        Map<String, Value> context = new HashMap<>();
        context.put("argument2", ValueFactory.create("3"));
        context.put("argument1", ValueFactory.create("5"));
        Argument argument = new Argument("argument1", ValueFactory.create("${ argument2 }"));
        List<Argument> arguments = Collections.singletonList(argument);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("3", result.get("argument1").get());
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = RuntimeException.class)
    public void testExpressionWithWrongRef() {
        Map<String, Value> context = new HashMap<>();

        Argument argument = new Argument("argument1", ValueFactory.create("${ argument2 }"));
        List<Argument> arguments = Collections.singletonList(argument);

        bindArguments(arguments, context);
    }

    @Test
    public void testArgumentAssignFromAnotherArgument() {
        Map<String, Value> context = new HashMap<>();

        Argument argument1 = new Argument("argument1", ValueFactory.create("5"));
        Argument argument2 = new Argument("argument2", ValueFactory.create("${ argument1 }"));
        List<Argument> arguments = Arrays.asList(argument1, argument2);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("5", result.get("argument1").get());
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals("5", result.get("argument2").get());
        Assert.assertEquals(2, result.size());

        Assert.assertTrue("orig context should not change", context.isEmpty());
    }

    @Test
    public void testComplexExpressionArgument() {
        Map<String, Value> context = new HashMap<>();
        context.put("varX", ValueFactory.create("5"));

        Argument argument1 = new Argument("argument1", ValueFactory.create("5"));
        Argument argument2 = new Argument("argument2", ValueFactory.create("${ argument1 + \"5\" + varX }"));
        List<Argument> arguments = Arrays.asList(argument1, argument2);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("5", result.get("argument1").get());
        Assert.assertTrue(result.containsKey("argument2"));
        Assert.assertEquals("555", result.get("argument2").get());
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("orig context should not change", 1, context.size());
    }

    @Test
    public void testComplexExpression2Argument() {
        Map<String, Value> context = new HashMap<>();
        context.put("varX", ValueFactory.create("roles"));

        Argument argument1 = new Argument("argument1", ValueFactory.create("${ 'mighty' + ' max '   + varX }"));
        List<Argument> arguments = Collections.singletonList(argument1);

        Map<String, Value> result = bindArguments(arguments, context);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("argument1"));
        Assert.assertEquals("mighty max roles", result.get("argument1").get());
        Assert.assertEquals(1, result.size());

        Assert.assertEquals("orig context should not change", 1, context.size());
    }

    @Test
    public void testArgumentWithPromptExpressions() {
        Map<String, Value> context = new HashMap<>();
        context.put("messageContainer1", ValueFactory.create("(What's the story?)"));
        context.put("messageContainer2", ValueFactory.create("Hey hey!"));
        context.put("messageContainer3", ValueFactory.create("Rock 'n' Roll"));
        context.put("singleChoiceDelimiter", ValueFactory.create("|"));
        context.put("singleChoiceOptions", ValueFactory.create("1|2|3"));
        context.put("multiChoiceDelimiter", ValueFactory.create("!"));
        context.put("multiChoiceOptions", ValueFactory.create("x!y!z"));

        Prompt textPrompt = new Prompt.PromptBuilder()
                .setPromptType(PromptType.TEXT)
                .setPromptMessage("${messageContainer1 + ' Morning glory'}")
                .build();

        Prompt singleChoicePrompt = new Prompt.PromptBuilder()
                .setPromptType(PromptType.SINGLE_CHOICE)
                .setPromptMessage("${messageContainer2 + ' My my!'}")
                .setPromptOptions("${singleChoiceOptions}")
                .setPromptDelimiter("${singleChoiceDelimiter}")
                .build();

        Prompt multiChoicePrompt = new Prompt.PromptBuilder()
                .setPromptType(PromptType.MULTI_CHOICE)
                .setPromptMessage("${messageContainer3 + ' will never die'}")
                .setPromptOptions("${multiChoiceOptions}")
                .setPromptDelimiter("${multiChoiceDelimiter}")
                .build();

        Argument argument1 = new Argument("argument1",
                null,
                Collections.emptySet(),
                Collections.emptySet(),
                false,
                textPrompt);

        Argument argument2 = new Argument("argument2",
                null,
                Collections.emptySet(),
                Collections.emptySet(),
                false,
                singleChoicePrompt);

        Argument argument3 = new Argument("argument3",
                null,
                Collections.emptySet(),
                Collections.emptySet(),
                false,
                multiChoicePrompt);


        Map<String, Value> result = bindArguments(Arrays.asList(argument1, argument2, argument3), context);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("argument1"));
        assertTrue(result.containsKey("argument2"));
        assertTrue(result.containsKey("argument3"));

        assertEquals("(What's the story?) Morning glory", argument1.getPrompt().getPromptMessage());

        assertEquals("Hey hey! My my!", argument2.getPrompt().getPromptMessage());
        assertEquals("|", argument2.getPrompt().getPromptDelimiter());
        assertEquals("1|2|3", argument2.getPrompt().getPromptOptions());

        assertEquals("Rock 'n' Roll will never die", argument3.getPrompt().getPromptMessage());
        assertEquals("!", argument3.getPrompt().getPromptDelimiter());
        assertEquals("x!y!z", argument3.getPrompt().getPromptOptions());
    }

    private Map<String, Value> bindArguments(
            List<Argument> arguments,
            Map<String, ? extends Value> context) {
        return argumentsBinding.bindArguments(arguments, context, EMPTY_SET);
    }

    private Map<String, Value> bindArguments(List<Argument> arguments) {
        return bindArguments(arguments, new HashMap<String, Value>());
    }

    @Configuration
    static class Config {

        @Bean
        public ArgumentsBinding argumentsBinding() {
            return new ArgumentsBinding();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator() {
            return new ScriptEvaluator();
        }

        @Bean
        public ScriptsService scriptsService() {
            return new ScriptsService();
        }

        @Bean
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }

        @Bean(name = "jythonRuntimeService")
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean(name = "jythonExecutionEngine")
        public PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine();
        }

        @Bean(name = "pythonExecutorConfigurationDataService")
        public PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService() {
            return PythonExecutorDetails::new;
        }

        @Bean(name = "pythonExecutorCommunicationService")
        public PythonExecutorCommunicationService pythonExecutorCommunicationService() {
            return mock(PythonExecutorCommunicationService.class);
        }

        @Bean(name = "pythonExecutorLifecycleManagerService")
        public PythonExecutorLifecycleManagerService pythonExecutorLifecycleManagerService() {
            return new PythonExecutorLifecycleManagerServiceImpl(pythonExecutorCommunicationService(),
                    pythonExecutorConfigurationDataService());
        }

        @Bean(name = "externalPythonExecutorService")
        public PythonRuntimeService externalPythonExecutorService() {
            return new ExternalPythonExecutorServiceImpl(new Semaphore(100), new Semaphore(50));
        }

        @Bean(name = "externalPythonRuntimeService")
        public PythonRuntimeService externalPythonRuntimeService() {
            return new ExternalPythonRuntimeServiceImpl(new Semaphore(100), new Semaphore(50));
        }

        @Bean(name = "externalPythonExecutionEngine")
        public PythonExecutionEngine externalPythonExecutionEngine() {
            return new ExternalPythonExecutionEngine();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
