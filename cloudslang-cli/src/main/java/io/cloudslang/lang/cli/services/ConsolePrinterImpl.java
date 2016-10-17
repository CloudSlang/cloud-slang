/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.services;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Service;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Created by bancl on 10/17/2016.
 */
@Service
public class ConsolePrinterImpl implements ConsolePrinter {

    @Override
    public void printWithColor(Ansi.Color color, String msg) {
        AnsiConsole.out().print(ansi().fg(color).a(msg).newline());
        AnsiConsole.out().print(ansi().fg(Ansi.Color.WHITE));
    }

}
