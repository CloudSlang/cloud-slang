package com.hp.score.lang.compiler.utils;
/*
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

/*
 * Created by orius123 on 05/11/14.
 */

import ch.lambdaj.Lambda;
import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.CompiledExecutable;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledTask;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

@Component
public class DependenciesHelper {

    /**
     * fetches all of the slang files from the given path
     *
     * @param path the path to look in
     * @return a List of {@link java.io.File} that has slang extensions
     */
    public List<File> fetchSlangFiles(Set<File> path) {
        return filterFiles(path, Arrays.asList(System.getProperty("slang.extensions", "yaml,yml,sl").split(",")));
    }

//    /**
//     * fetches all of the script files from the given path
//     *
//     * @param path the path to look in
//     * @return a List of {@link java.io.File} that has script extensions (currently only python)
//     */
//    public List<File> fetchScriptFiles(Set<File> path) {
//        return filterFiles(path, Arrays.asList(System.getProperty("script.extensions", "py").split(",")));
//    }

    /**
     * filter a path by extension
     *
     * @param path       the path to look in
     * @param extensions List of String to filter by
     * @return a List of {@link java.io.File} that has the requested extensions
     */
    private List<File> filterFiles(Set<File> path, List<String> extensions) {
        List<File> filteredClassPath = new ArrayList<>();
        for (File file : path) {
            if (file.isDirectory()) {
                filteredClassPath.addAll(FileUtils.listFiles(file, extensions.toArray(new String[extensions.size()]), true));
            } else {
                if (extensions.contains(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                    filteredClassPath.add(file);
                }
            }
        }
        return filteredClassPath;
    }

    /**
     * recursive matches executables with their references
     *
     * @param availableDependencies the executables to match from
     * @return a map of a the executables that were successfully matched
     */
    public Map<String, CompiledExecutable> matchReferences(CompiledExecutable executable, Collection<CompiledExecutable> availableDependencies) {
        Validate.isTrue(executable.getType().equals(SlangTextualKeys.FLOW_TYPE), "Executable: " + executable.getId() + " is not a flow, therefore it has no references");
        Map<String, CompiledExecutable> resolvedDependencies = new HashMap<>();
        return fetchFlowReferences((CompiledFlow) executable, availableDependencies, resolvedDependencies);
    }

    private Map<String, CompiledExecutable> fetchFlowReferences(CompiledFlow flow,
                                                                Collection<CompiledExecutable> availableDependencies,
                                                                Map<String, CompiledExecutable> resolvedDependencies) {
        Deque<CompiledTask> tasks = flow.getCompiledWorkflow().getCompiledTasks();
        for (CompiledTask task : tasks) {
            String refId = task.getRefId();
            //if it is already in the references we do nothing
            if (resolvedDependencies.get(refId) == null) {
                CompiledExecutable matchingRef = Lambda.selectFirst(availableDependencies, having(on(CompiledExecutable.class).getId(), equalTo(refId)));
                if (matchingRef == null) {
                    throw new RuntimeException("Reference: " + refId + " of task: " + task.getName() + " in flow: "
                            + flow.getName() + "wasn't found in path");
                }
                validateNavigation(task, matchingRef);
                //first we put the reference on the map
                resolvedDependencies.put(matchingRef.getId(), matchingRef);
                if (matchingRef.getType().equals(SlangTextualKeys.FLOW_TYPE)) {
                    //if it is a flow  we recursively
                    resolvedDependencies.putAll(fetchFlowReferences((CompiledFlow) matchingRef, availableDependencies, resolvedDependencies));
                }
            }
        }
        return resolvedDependencies;
    }

    private void validateNavigation(CompiledTask task, CompiledExecutable matchingRef) {
        Validate.notEmpty(matchingRef.getResults());
        Validate.notEmpty(task.getNavigationStrings());
    }
}
