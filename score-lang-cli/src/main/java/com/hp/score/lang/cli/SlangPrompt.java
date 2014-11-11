package com.hp.score.lang.cli;

import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */
@Component
public class SlangPrompt implements PromptProvider {
    private static final String prompt = "slang>";
    private static final String name = "Slang";
    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public String name() {
        return name;
    }
}
