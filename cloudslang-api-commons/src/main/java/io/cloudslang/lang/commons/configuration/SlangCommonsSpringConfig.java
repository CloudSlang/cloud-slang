package io.cloudslang.lang.commons.configuration;

import io.cloudslang.lang.api.configuration.SlangSpringConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Bonczidai Levente
 * @since 8/23/2016
 */
@Configuration
@Import({SlangSpringConfiguration.class})
@ComponentScan("io.cloudslang.lang.commons")
public class SlangCommonsSpringConfig {
}
