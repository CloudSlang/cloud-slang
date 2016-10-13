package io.cloudslang.lang.api.configuration;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.api.SlangImpl;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.runtime.configuration.SlangRuntimeSpringConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * User: stoneo
 * Date: 03/12/2014
 * Time: 10:39
 */
@Configuration
@Import({SlangRuntimeSpringConfig.class, SlangCompilerSpringConfig.class})
public class SlangSpringConfiguration {

    @Bean
    public Slang slang() {
        return new SlangImpl();
    }

}
