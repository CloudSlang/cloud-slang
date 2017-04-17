/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.cli.configuration.liquibase;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.StopWatch;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * @author Bonczidai Levente
 * @since 12/15/2015
 */
public class AsyncSpringLiquibase extends SpringLiquibase {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    @Qualifier("scoreOrchestratorScheduler")
    private TaskExecutor taskExecutor;
//
//    @Autowired
//    private Environment environment;

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        System.out.println("Test-async");
        System.out.println("Thread nr - afterPropertiesSet: " + Thread.currentThread().getId());
//        new Thread((new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    System.out.println("Thread nr - afterPropertiesSet.run: " + Thread.currentThread().getId());
//                    initDb();
//                } catch (LiquibaseException e) {
//                    e.printStackTrace();
//                }
//            }
//        })).start();
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Thread nr - afterPropertiesSet.run: " + Thread.currentThread().getId());
                    initDb();
                } catch (LiquibaseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void initDb() throws LiquibaseException {
        StopWatch watch = new StopWatch();
        watch.start();
        System.out.println("Thread nr - initDb: " + Thread.currentThread().getId());
        super.afterPropertiesSet();
        watch.stop();
        String msg = "Started Liquibase in " + watch.getTotalTimeMillis() + " ms";
        System.out.println(msg);
    }

}
