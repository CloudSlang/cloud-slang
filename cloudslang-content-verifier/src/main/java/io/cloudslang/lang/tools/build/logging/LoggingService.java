package io.cloudslang.lang.tools.build.logging;


import org.apache.log4j.Level;

import java.util.concurrent.Future;

public interface LoggingService {

    Future<?> logEvent(final Level level, final String message);
    Future<?> logEvent(final Level level, final String message, final Throwable throwable);

}
