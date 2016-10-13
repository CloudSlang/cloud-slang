package io.cloudslang.lang.cli;

import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */
@Component
@Order(Integer.MIN_VALUE)
public class SlangPrompt extends SlangNamedProvider implements PromptProvider {

    @Override
    public String getPrompt() {
        return "cslang>";
    }

}
