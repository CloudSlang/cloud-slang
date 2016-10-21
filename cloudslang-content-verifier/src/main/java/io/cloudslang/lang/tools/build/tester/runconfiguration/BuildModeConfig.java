/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.runconfiguration;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.tools.build.SlangBuildMain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 10/10/2016
 */
public class BuildModeConfig {
    private final SlangBuildMain.BuildMode buildMode;
    private final Set<String> changedFiles;
    private final Map<String, Executable> allTestedFlowModels;

    private BuildModeConfig(SlangBuildMain.BuildMode buildMode, Set<String> changedFiles,
                            Map<String, Executable> allTestedFlowModels) {
        this.buildMode = buildMode;
        this.changedFiles = changedFiles;
        this.allTestedFlowModels = allTestedFlowModels;
    }

    public static BuildModeConfig createBasicBuildModeConfig() {
        return new BuildModeConfig(SlangBuildMain.BuildMode.BASIC, new HashSet<String>(),
                new HashMap<String, Executable>());
    }

    public static BuildModeConfig createChangedBuildModeConfig(Set<String> changedFiles,
                                                               Map<String, Executable> allTestedFlowModels) {
        return new BuildModeConfig(SlangBuildMain.BuildMode.CHANGED, changedFiles, allTestedFlowModels);
    }

    public SlangBuildMain.BuildMode getBuildMode() {
        return buildMode;
    }

    public Set<String> getChangedFiles() {
        return changedFiles;
    }

    public Map<String, Executable> getAllTestedFlowModels() {
        return allTestedFlowModels;
    }
}
