/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.cli;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.stereotype.Component;

/**
 * @author lesant
 * @since 11/07/2014
 * @version $Id$
 */
@Component
@Order(Integer.MIN_VALUE)
public class SlangBanner extends SlangNamedProvider implements BannerProvider {

	private static final String BANNER = "slangBanner.txt";
	private static final String ASSISTANCE = "Welcome to Slang. For assistance type help.";

	@Override
	public String getBanner() {
		StringBuilder sb = new StringBuilder();
		try (InputStream in = ClassLoader.getSystemResourceAsStream(BANNER)) {
			sb.append(IOUtils.toString(in));
		} catch(IOException e) {
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
		return ASSISTANCE;
	}

}
