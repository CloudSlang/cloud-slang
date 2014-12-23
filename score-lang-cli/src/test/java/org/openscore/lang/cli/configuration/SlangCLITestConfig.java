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
package org.openscore.lang.cli.configuration;

import org.openscore.cli.services.ScoreServices;
import org.openscore.cli.services.ScoreServicesImpl;
import org.openscore.cli.utils.CompilerHelper;
import org.openscore.cli.utils.CompilerHelperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
@Configuration
@ComponentScan( "com.hp.score.lang.cli" )
public class SlangCLITestConfig {

    @Bean
    public ScoreServices scoreServices() {
        return mock(ScoreServicesImpl.class);
    }

    @Bean
    public CompilerHelper compilerHelper() throws IOException {
        return mock(CompilerHelperImpl.class);
    }

}
