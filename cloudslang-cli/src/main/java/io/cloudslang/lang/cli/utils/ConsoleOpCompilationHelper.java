/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.cli.services.ConsolePrinter;
import io.cloudslang.lang.commons.services.api.CompilationHelper;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.Future;

@Component
public class ConsoleOpCompilationHelper implements CompilationHelper {

    @Autowired
    private ConsolePrinter consolePrinter;

    @Override
    public void onCompilationFinish() {
        consolePrinter.waitForAllPrintTasksToFinish();
    }

    @Override
    public Future<?> onEveryFile(File file) {
        return consolePrinter.printWithColor(Ansi.Color.GREEN, "Compiling " + file.getName());
    }
}
