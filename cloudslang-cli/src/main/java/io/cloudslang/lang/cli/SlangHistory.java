package io.cloudslang.lang.cli;

import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.stereotype.Component;

/**
 * @author lesant
 * @version $Id$
 * @since 11/07/2014
 */
@Component
@Order(Integer.MIN_VALUE)
public class SlangHistory extends SlangNamedProvider implements HistoryFileNameProvider {

    @Override
    public String getHistoryFileName() {
        return System.getProperty("app.home") + "/cslang-cli.history";
    }

}
