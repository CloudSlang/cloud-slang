/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.impl;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.CompilationHelper;
import io.cloudslang.lang.commons.services.api.SlangCompilationService;
import io.cloudslang.lang.compiler.PrecompileStrategy;
import io.cloudslang.lang.compiler.SlangSource;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilationServiceImplTest.Config.class)
public class SlangCompilationServiceImplTest {

    @Autowired
    private SlangCompilationService slangCompilationService;

    @Autowired
    private CompilationHelper compilationHelper;

    @Autowired
    private Slang slang;

    @Test
    public void testLoadInputsFromFile() throws URISyntaxException {
        File folder = new File(getClass().getResource("/executables").toURI().getPath());
        Collection<File> files = slangCompilationService.listSlangFiles(folder, true);
        assertEquals(4, files.size());
    }

    @Test
    public void testCompileFoldersCleanup() throws Exception {
        List<String> folders = new ArrayList<>();
        folders.add(getClass().getResource("/executables").toURI().getPath());
        slangCompilationService.compileFolders(folders, compilationHelper);

        File file1 = new File(getClass().getResource("/executables/dir1/flow2.sl").toURI());
        File file2 = new File(getClass().getResource("/executables/dir2/flowprop.sl").toURI());
        File file3 = new File(getClass().getResource("/executables/dir3/dir3_1/test_op.sl").toURI());
        File file4 = new File(getClass().getResource("/executables/dir1/flow2.sl").toURI());

        verify(compilationHelper).onEveryFile(file1);
        verify(compilationHelper).onEveryFile(file2);
        verify(compilationHelper).onEveryFile(file3);
        verify(compilationHelper).onEveryFile(file4);

        InOrder inOrderHelper = inOrder(compilationHelper);
        inOrderHelper.verify(compilationHelper, atLeastOnce()).onEveryFile(any(File.class));
        inOrderHelper.verify(compilationHelper).onCompilationFinish();
        inOrderHelper.verifyNoMoreInteractions();

        InOrder inOrder = inOrder(slang);
        inOrder.verify(slang, atLeastOnce()).compileSource(
                any(SlangSource.class), any(Set.class), eq(PrecompileStrategy.WITH_CACHE));
        inOrder.verify(slang).invalidateAllInPreCompileCache();
        inOrder.verifyNoMoreInteractions();
    }

    @Configuration
    static class Config {

        @Bean
        public Slang slang() {
            return mock(Slang.class);
        }

        @Bean
        public SlangCompilationService slangCompilationService() {
            return new SlangCompilationServiceImpl();
        }

        @Bean
        public CompilationHelper compilationHelper() {
            return mock(CompilationHelper.class);
        }

    }
}