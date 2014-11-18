/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.cli;

import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * Date: 11/7/2014
 * @author lesant
 */
@Component
@Order(Integer.MIN_VALUE)
public class SlangPrompt extends SlangNamedProvider implements PromptProvider {

	@Override
	public String getPrompt() {
		return "slang>";
	}

}
