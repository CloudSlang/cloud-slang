package liquibase.ext.logging;/*
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
