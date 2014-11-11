package com.hp.score.lang.cli;

import org.springframework.shell.plugin.BannerProvider;
import org.springframework.stereotype.Component;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */
@Component
public class SlangBanner implements BannerProvider{

    private static final String assistance = "Welcome to Slang. For assistance type help.";
    private static final String banner = "  _________.__                  ________ \n" +
            " /   _____/|  | _____    ____  /  _____/ \n" +
            " \\_____  \\ |  | \\__  \\  /    \\/   \\  ___ \n" +
            " /        \\|  |__/ __ \\|   |  \\    \\_\\  \\\n" +
            "/_______  /|____(____  /___|  /\\______  /\n" +
            "        \\/           \\/     \\/        \\/ ";
    @Override
    public String getBanner() {
        return banner + "\n"+ SlangCLI.getVersion();
    }

    @Override
    public String getVersion() {
        return SlangCLI.getVersion();
    }

    @Override
    public String getWelcomeMessage() {
        return assistance;
    }

    @Override
    public String name() {
        return "Slang " + getVersion() + " - SpringShell";
    }
}
