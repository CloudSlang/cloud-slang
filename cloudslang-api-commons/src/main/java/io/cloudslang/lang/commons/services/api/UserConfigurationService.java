package io.cloudslang.lang.commons.services.api;

import java.io.IOException;

/**
 * @author Bonczidai Levente
 * @since 8/22/2016
 */
public interface UserConfigurationService {
    void loadUserProperties() throws IOException;
}
