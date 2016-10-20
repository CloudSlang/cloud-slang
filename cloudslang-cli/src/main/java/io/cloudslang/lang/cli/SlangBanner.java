/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.stereotype.Component;

/**
 * @author lesant
 * @version $Id$
 * @since 11/07/2014
 */
@Component
@Order(Integer.MIN_VALUE)
public class SlangBanner extends SlangNamedProvider implements BannerProvider {

    private static final String BANNER = "slangBanner.txt";
    private static final String ASSISTANCE = "Welcome to CloudSlang. For assistance type help.";

    @Value("${slang.version}")
    private String slangVersion;

    @Override
    public String getBanner() {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = ClassLoader.getSystemResourceAsStream(BANNER)) {
            sb.append(IOUtils.toString(in));
        } catch (IOException e) {
            sb.append("CloudSlang");
        }
        sb.append(System.lineSeparator());
        sb.append(getVersion());
        return sb.toString();
    }

    @Override
    public String getVersion() {
        return slangVersion;
    }

    @Override
    public String getWelcomeMessage() {
        return ASSISTANCE;
    }

}
