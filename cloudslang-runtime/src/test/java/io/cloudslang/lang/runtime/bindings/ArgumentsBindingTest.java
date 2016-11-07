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
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionNotCachedEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

import static org.junit.Assert.assertNull;

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
        System.setProperty("maven.home", classLoader.getResource("maven").getPath());

        shouldRunMaven = System.getProperties().containsKey(MavenConfigImpl.MAVEN_REMOTE_URL) &&
                System.getProperties().containsKey(MavenConfigImpl.MAVEN_PLUGINS_URL);

        System.setProperty(MavenConfigImpl.MAVEN_SETTINGS_PATH, settingsXmlPath);
        System.setProperty(MavenConfigImpl.MAVEN_M2_CONF_PATH, classLoader.getResource("m2.conf").getPath());

        String provideralAlreadyConfigured = System.setProperty("python.executor.engine",
                PythonExecutionNotCachedEngine.class.getSimpleName());
        assertNull("python.executor.engine was configured before this test!!!!!!!", provideralAlreadyConfigured);
    }

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Autowired
    private ArgumentsBinding argumentsBinding;

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }

        @Bean
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean
        public PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
