/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.logging;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.PostConstruct;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.FATAL;
import static org.apache.log4j.Level.INFO;
import static org.apache.log4j.Level.TRACE;
import static org.apache.log4j.Level.WARN;

/**
 * Implements a logging service that is asynchronous with respect to the caller
 */
public class LoggingServiceImpl implements LoggingService, DisposableBean {
    private static final Logger logger = Logger.getLogger(LoggingServiceImpl.class);

    private ThreadPoolExecutor singleThreadExecutor;

    @PostConstruct
    public void initialize() {
        singleThreadExecutor = (ThreadPoolExecutor) newFixedThreadPool(1); // one thread only
    }

    @Override
    public Future<?> logEvent(Level level, String message) {
        return doLogEvent(level, message, null);
    }

    @Override
    public Future<?> logEvent(Level level, String message, Throwable throwable) {
        return doLogEvent(level, message, throwable);
    }

    @Override
    public void destroy() throws Exception {
        singleThreadExecutor.shutdown();
        singleThreadExecutor = null;
    }

    private Future<?> doLogEvent(Level level, String message, Throwable throwable) {
        try {
            return doSubmit(level, message, throwable);
        } catch (RejectedExecutionException rejectedExecutionException) {
            try {
                return doSubmit(level, message, throwable);
            } catch (RejectedExecutionException rejectedExecutionException2) {
                // try twice and fail
            }
        }
        return null;
    }

    private Future<?> doSubmit(Level level, String message, Throwable throwable) {
        return singleThreadExecutor.submit((throwable != null) ? new LoggingDetailsRunnable(level, message, throwable)
                : new LoggingDetailsRunnable(level, message));
    }

    @Override
    public void waitForAllLogTasksToFinish() {
        try {
            singleThreadExecutor.shutdown();
            singleThreadExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignore) {
        }
    }

    static class LoggingDetailsRunnable implements Runnable {
        private final Level level;
        private final String message;
        private final Throwable exception;

        LoggingDetailsRunnable(Level level, String message, Throwable t) {
            this.level = level;
            this.message = message;
            this.exception = t;
        }

        LoggingDetailsRunnable(Level level, String message) {
            this(level, message, null);
        }

        @Override
        public void run() {
            try {
                String localMessage = StringUtils.isEmpty(message) ? "" : message;
                final Throwable throwable = exception;
                if (exception == null) {
                    if (level == TRACE) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(localMessage);
                        }
                    } else if (level == DEBUG) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(localMessage);
                        }
                    } else if (level == INFO) {
                        logger.info(localMessage);
                    } else if (level == WARN) {
                        logger.warn(localMessage);
                    } else if (level == ERROR) {
                        logger.error(localMessage);
                    } else if (level == FATAL) {
                        logger.fatal(localMessage);
                    }
                } else {
                    if (level == TRACE) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(localMessage, throwable);
                        }
                    } else if (level == DEBUG) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(localMessage, throwable);
                        }
                    } else if (level == INFO) {
                        logger.info(localMessage, throwable);
                    } else if (level == WARN) {
                        logger.warn(localMessage, throwable);
                    } else if (level == ERROR) {
                        logger.error(localMessage, throwable);
                    } else if (level == FATAL) {
                        logger.fatal(localMessage, throwable);
                    }
                }
            } catch (Exception ignore) {
                // so that this thread does not die
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            LoggingDetailsRunnable rhs = (LoggingDetailsRunnable) obj;
            return new EqualsBuilder()
                    .append(this.level, rhs.level)
                    .append(this.message, rhs.message)
                    .append(this.exception, rhs.exception)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(level)
                    .append(message)
                    .append(exception)
                    .toHashCode();
        }
    }

}
