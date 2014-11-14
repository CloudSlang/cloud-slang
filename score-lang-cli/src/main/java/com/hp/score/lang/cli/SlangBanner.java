package com.hp.score.lang.cli;

import org.apache.commons.io.IOUtils;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */
@Component
public class SlangBanner implements BannerProvider {

    private static final String assistance = "Welcome to Slang. For assistance type help.";

    private static final String banner = "slangBanner.txt";

    @Override
    public String getBanner() {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = ClassLoader.getSystemResourceAsStream(banner)) {
            sb.append(IOUtils.toString(in));
        } catch (IOException e) {
            sb.append("Slang");
        }
        sb.append(System.lineSeparator());
        sb.append(SlangCLI.getVersion());

        return sb.toString();
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
        return "Slang-";
    }
}
