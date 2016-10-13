package io.cloudslang.lang.cli;

import io.cloudslang.lang.commons.services.api.UserConfigurationService;
import io.cloudslang.lang.commons.services.impl.UserConfigurationServiceImpl;
import java.io.IOException;
import org.springframework.shell.Bootstrap;

/**
 * @author Bonczidai Levente
 * @since 12/16/2015
 */
public class SlangBootstrap {

    public static void main(String[] args) throws IOException {
        loadUserProperties();
        System.out.println("Loading..");
        Bootstrap.main(args);
    }

    @SuppressWarnings("Duplicates")
    private static void loadUserProperties() {
        try {
            UserConfigurationService userConfigurationService = new UserConfigurationServiceImpl();
            userConfigurationService.loadUserProperties();
        } catch (Exception ex) {
            System.out.println("Error occurred while loading user configuration: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
