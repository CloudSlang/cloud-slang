package io.cloudslang.lang.cli;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * User: bancl
 * Date: 7/4/2016
 */
public class SlangBannerTest {

    public static final String BANNER = "_________ .__                   .____________.__" + System.lineSeparator() +
            "\\_   ___ \\|  |   ____  __ __  __| _/   _____/|  | _____    ____    ____" + System.lineSeparator() +
            "/    \\  \\/|  |  /  _ \\|  |  \\/ __ |\\_____  \\ |  | \\__  \\  /    \\  / ___\\" + System.lineSeparator() +
            "\\     \\___|  |_(  <_> )  |  / /_/ |/        \\|  |__/ __ \\|   |  \\/ /_/  >" + System.lineSeparator() +
            " \\______  /____/\\____/|____/\\____ /_______  /|____(____  /___|  /\\___  /" + System.lineSeparator() +
            "        \\/                       \\/       \\/           \\/     \\//_____/" + System.lineSeparator() +
            "null";
    public static final String CLOUDSLANG = "CloudSlang";
    private SlangBanner slangBanner;

    @Before
    public void before() throws Exception {
        slangBanner = new SlangBanner();
    }

    @Test
    public void testGetBanner() throws Exception {
        Assert.assertEquals(BANNER, slangBanner.getBanner());
    }

    @Test
    public void testGetProviderName() {
        Assert.assertEquals(CLOUDSLANG, slangBanner.getProviderName());
    }
}
