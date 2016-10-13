package io.cloudslang.lang.cli;

import org.springframework.shell.plugin.NamedProvider;

/**
 * @author moradi
 * @version $Id$
 * @since 18/11/2014
 */
public abstract class SlangNamedProvider implements NamedProvider {

    @Override
    public String getProviderName() {
        return "CloudSlang";
    }

}
