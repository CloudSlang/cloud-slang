/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Colour-coded console appender for Log4J.
 */
public class ANSIConsoleAppender extends ConsoleAppender
{
    private static final int NORMAL = 0;
    private static final int BRIGHT = 1;
    private static final int FOREGROUND_RED = 31;
    private static final int FOREGROUND_YELLOW = 33;

    private static final String PREFIX = "\u001b[";
    private static final String SUFFIX = "m";
    private static final char SEPARATOR = ';';
    private static final String END_COLOUR = PREFIX + SUFFIX;

    private static final String FATAL_COLOUR = PREFIX
            + BRIGHT + SEPARATOR + FOREGROUND_RED + SUFFIX;
    private static final String ERROR_COLOUR = PREFIX
            + NORMAL + SEPARATOR + FOREGROUND_RED + SUFFIX;
    private static final String WARN_COLOUR = PREFIX
            + NORMAL + SEPARATOR + FOREGROUND_YELLOW + SUFFIX;

    /**
     * Wraps the ANSI control characters around the
     * output from the super-class Appender.
     */
    protected void subAppend(LoggingEvent event)
    {
        this.qw.write(getColour(event.getLevel()));
        super.subAppend(event);
        this.qw.write(END_COLOUR);

        if(this.immediateFlush)
        {
            this.qw.flush();
        }
    }

    /**
     * Get the appropriate control characters to change
     * the colour for the specified logging level.
     */
    private String getColour(Level level)
    {
        switch (level.toInt())
        {
            case Priority.FATAL_INT: return FATAL_COLOUR;
            case Priority.ERROR_INT: return ERROR_COLOUR;
            case Priority.WARN_INT: return WARN_COLOUR;
            default: return END_COLOUR;
        }
    }
}
