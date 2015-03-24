/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package liquibase.ext.logging;

import liquibase.logging.core.AbstractLogger;
import org.springframework.stereotype.Component;

@Component
public class LiquibaseLogger  extends AbstractLogger {
    /* This class is ONLY to fix Liquibase default logger issues.. */

    private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LiquibaseLogger.class);

    private String name = "";

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setLogLevel(String logLevel, String logFile) {}

    @Override
    public void severe(String message) {
        logger.error(message);
    }

    @Override
    public void severe(String message, Throwable e) {
        logger.error(message,e);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }

    @Override
    public void warning(String message, Throwable e) {
        logger.warn(message,e);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Throwable e) {
        logger.info(message,e);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable e) {
        logger.debug(message,e);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
