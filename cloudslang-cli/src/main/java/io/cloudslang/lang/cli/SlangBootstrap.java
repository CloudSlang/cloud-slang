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
package io.cloudslang.lang.cli;

import org.springframework.shell.Bootstrap;

import java.io.IOException;

/**
 * @author Bonczidai Levente
 * @since 12/16/2015
 */
public class SlangBootstrap {

    public static void main(String[] args) throws IOException {
        System.out.println("Loading..");
        Bootstrap.main(args);
    }

}
