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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SlangBannerTest {
    public static final String CLOUDSLANG = "CloudSlang";
    private SlangBanner slangBanner;

    @Before
    public void before() throws Exception {
        slangBanner = new SlangBanner();
    }

    @Test
    public void testGetBanner() throws Exception {
        String banner = IOUtils.toString(getClass().getResource("/slangBanner1.txt").toURI());
        Assert.assertTrue(slangBanner.getBanner().contains(banner));
    }

    @Test
    public void testGetProviderName() {
        Assert.assertEquals(CLOUDSLANG, slangBanner.getProviderName());
    }
}
