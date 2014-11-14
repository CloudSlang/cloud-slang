package com.hp.score.lang.cli;

import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.stereotype.Component;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */
@Component
public class SlangHistory implements HistoryFileNameProvider{
    private static final String historyFileName = "slang.log";
    private static final String name = "slang log";
    @Override
    public String getHistoryFileName() {
        return historyFileName;
    }

    @Override
    public String name() {
        return name;
    }
}
