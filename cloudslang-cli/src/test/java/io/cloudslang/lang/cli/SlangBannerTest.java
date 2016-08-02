package io.cloudslang.lang.cli;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * User: bancl
 * Date: 7/4/2016
 */
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
