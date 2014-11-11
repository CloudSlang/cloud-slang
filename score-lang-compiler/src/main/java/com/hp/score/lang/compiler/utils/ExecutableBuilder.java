package com.hp.score.lang.compiler.utils;/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

import ch.lambdaj.Lambda;
import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.*;
import com.hp.score.lang.compiler.transformers.Transformer;
import com.hp.score.lang.entities.ScoreLangConstants;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.*;

/*
 * Created by orius123 on 09/11/14.
 */
@Component
public class ExecutableBuilder {

    @Autowired
    private List<Transformer> transformers;

    public CompiledExecutable compileExecutable(Map<String, Object> executableRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace, SlangFile.Type type) {
        Map<String, Serializable> preExecutableActionData = new HashMap<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();
        CompiledDoAction compiledDoAction = null;
        CompiledWorkflow compiledWorkflow = null;

        List<Transformer> preExecTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_EXECUTABLE)), transformers);
        List<Transformer> postExecTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_EXECUTABLE)), transformers);

        validateKeyWordsExits(executableRawData, ListUtils.union(preExecTransformers, postExecTransformers), Arrays.asList(SlangTextualKeys.ACTION_KEY, SlangTextualKeys.WORKFLOW_KEY));

        Iterator<Map.Entry<String, Object>> it = executableRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();

            if (key.equals(SlangTextualKeys.ACTION_KEY)) {
                @SuppressWarnings("unchecked") Map<String, Object> actionRawData = (Map<String, Object>) pairs.getValue();
                compiledDoAction = compileAction(actionRawData);
            }

            if (key.equals(SlangTextualKeys.WORKFLOW_KEY)) {
                @SuppressWarnings("unchecked") LinkedHashMap<String, Map<String, Object>> workFlowRawData = (LinkedHashMap<String, Map<String, Object>>) pairs.getValue();
                compiledWorkflow = compileWorkFlow(workFlowRawData, dependenciesByNamespace);
            }

            preExecutableActionData.putAll(runTransformers(executableRawData, preExecTransformers, key));

            postExecutableActionData.putAll(runTransformers(executableRawData, postExecTransformers, key));

            it.remove();
        }

        if (type == SlangFile.Type.FLOW) {
            return new CompiledFlow(preExecutableActionData, postExecutableActionData, compiledWorkflow);
        } else {
            return new CompiledOperation(preExecutableActionData, postExecutableActionData, compiledDoAction);
        }
    }

    private CompiledDoAction compileAction(Map<String, Object> actionRawData) {
        Map<String, Serializable> actionData = new HashMap<>();

        List<Transformer> actionTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.ACTION)), transformers);

        validateKeyWordsExits(actionRawData, actionTransformers, null);

        Iterator<Map.Entry<String, Object>> it = actionRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();

            actionData.putAll(runTransformers(actionRawData, actionTransformers, key));

            it.remove();
        }
        return new CompiledDoAction(actionData);
    }

    private CompiledWorkflow compileWorkFlow(LinkedHashMap<String, Map<String, Object>> workFlowRawData, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        List<CompiledTask> compiledTasks = new ArrayList<>();

        Validate.notEmpty(workFlowRawData, "Flow must have tasks in its workflow");

        PeekingIterator<Map.Entry<String, Map<String, Object>>> iterator = new PeekingIterator<>(workFlowRawData.entrySet().iterator());

        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> taskRawData = iterator.next();
            Map.Entry<String, Map<String, Object>> nextTaskData = iterator.peek();
            String followingTaskName = nextTaskData != null ? nextTaskData.getKey() : null;
            CompiledTask compiledTask = compileTask(taskRawData.getKey(), taskRawData.getValue(), followingTaskName, dependenciesByNamespace);
            compiledTasks.add(compiledTask);
        }

        return new CompiledWorkflow(compiledTasks);
    }

    private CompiledTask compileTask(String taskName, Map<String, Object> taskRawData, String followingTaskName, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        Map<String, Serializable> preTaskData = new HashMap<>();
        Map<String, Serializable> postTaskData = new HashMap<>();
        Map<String, String> navigationStrings = null;
        String refId = null;

        List<Transformer> preTaskTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.BEFORE_TASK)), transformers);
        List<Transformer> postTaskTransformers = Lambda.filter(having(on(Transformer.class).getScopes().contains(Transformer.Scope.AFTER_TASK)), transformers);

        validateKeyWordsExits(taskRawData, ListUtils.union(preTaskTransformers, postTaskTransformers), Arrays.asList(SlangTextualKeys.DO_KEY, SlangTextualKeys.NAVIGATION_KEY));

        Iterator<Map.Entry<String, Object>> it = taskRawData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();

            if (key.equals(SlangTextualKeys.DO_KEY)) {
                @SuppressWarnings("unchecked") LinkedHashMap<String, Object> doRawData = (LinkedHashMap<String, Object>) entry.getValue();
                String refString = doRawData.keySet().iterator().next();
                refId = resolveRefId(refString, dependenciesByNamespace);
            }

            if (key.equals(SlangTextualKeys.NAVIGATION_KEY)) {
                navigationStrings = (Map<String, String>) entry.getValue();
            }

            preTaskData.putAll(runTransformers(taskRawData, preTaskTransformers, key));

            postTaskData.putAll(runTransformers(taskRawData, postTaskTransformers, key));

            it.remove();
        }

        //default navigation
        if (navigationStrings == null) {
            navigationStrings = new HashMap<>();
            navigationStrings.put(ScoreLangConstants.SUCCESS_RESULT, followingTaskName == null ? ScoreLangConstants.SUCCESS_RESULT : followingTaskName);
            navigationStrings.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
        }

        return new CompiledTask(taskName, preTaskData, postTaskData, navigationStrings, refId);
    }

    private String resolveRefId(String refIdString, TreeMap<String, List<SlangFile>> dependenciesByNamespace) {
        String importAlias = StringUtils.substringBefore(refIdString, ".");
        List<SlangFile> slangFilesList = dependenciesByNamespace.get(importAlias);
        List<Map> executableList = new ArrayList<>();
        for (SlangFile slangFile : slangFilesList) {
            executableList.addAll(slangFile.getOperations());
            Map<Object, Object> flow = new HashMap<>();
            flow.put(slangFile.getFlow().get(SlangTextualKeys.FLOW_NAME_KEY), null);
            executableList.add(flow);
        }
        String refIdSuffix = StringUtils.substringAfter(refIdString, ".");
        Map matchingExecutable = Lambda.selectFirst(executableList, hasKey(refIdSuffix));
        if (matchingExecutable == null) {
            throw new RuntimeException("No executable was found in the classpath that for ref: " + refIdString);
        }
        return slangFilesList.get(0).getNamespace() + "." + refIdSuffix;
    }

    private Map<String, Serializable> runTransformers(Map<String, Object> rawData, List<Transformer> transformers, String key) {
        Map<String, Serializable> transformedData = new HashMap<>();
        for (Transformer transformer : transformers) {
            if (shouldApplyTransformer(transformer, key)) {
                @SuppressWarnings("unchecked") Object value = transformer.transform(rawData.get(key));
                transformedData.put(key, (Serializable) value);
            }
        }
        return transformedData;
    }

    private void validateKeyWordsExits(Map<String, Object> operationRawData, List<Transformer> allRelevantTransformers, List<String> additionalValidKeyWords) {
        Set<String> validKeywords = new HashSet<>();

        if (additionalValidKeyWords != null) {
            validKeywords.addAll(additionalValidKeyWords);
        }

        for (Transformer transformer : allRelevantTransformers) {
            validKeywords.add(keyToTransform(transformer));
        }

        for (String key : operationRawData.keySet()) {
            if (!(Lambda.exists(validKeywords, equalToIgnoringCase(key)))) {
                throw new RuntimeException("No transformer were found for key: " + key);
            }
        }
    }

    private boolean shouldApplyTransformer(Transformer transformer, String key) {
        String transformerName = keyToTransform(transformer);
        return transformerName.equalsIgnoreCase(key);
    }

    private String keyToTransform(Transformer transformer) {
        String key;
        if (transformer.keyToTransform() != null) {
            key = transformer.keyToTransform();
        } else {
            String simpleClassName = transformer.getClass().getSimpleName();
            key = simpleClassName.substring(0, simpleClassName.indexOf("Transformer"));
        }
        return key;
    }
}
