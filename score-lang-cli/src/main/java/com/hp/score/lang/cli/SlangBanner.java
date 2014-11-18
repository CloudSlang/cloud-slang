package com.hp.score.lang.cli;

/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/


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
