package io.cloudslang.lang.cli.configuration;

import io.cloudslang.lang.cli.services.ScoreServices;
import io.cloudslang.lang.cli.services.ScoreServicesImpl;
import io.cloudslang.lang.cli.utils.CompilerHelper;
import io.cloudslang.lang.cli.utils.CompilerHelperImpl;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
@Configuration
@ComponentScan("io.cloudslang.lang.cli")
public class SlangCLITestConfig {

    @Bean
    public ScoreServices scoreServices() {
        return mock(ScoreServicesImpl.class);
    }

    @Bean
    public CompilerHelper compilerHelper() throws IOException {
        return mock(CompilerHelperImpl.class);
    }

    @Bean
    public SlangSourceService slangSourceService() {
        return mock(SlangSourceService.class);
    }
}
