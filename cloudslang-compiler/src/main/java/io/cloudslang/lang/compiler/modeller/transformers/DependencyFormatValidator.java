package io.cloudslang.lang.compiler.modeller.transformers;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 23/06/2016.
 */
@Component
class DependencyFormatValidator {
    private static final int DEPENDENCY_PARTS = 3;
    public static final String INVALID_DEPENDENCY = "Dependency definition should contain exactly [" + DEPENDENCY_PARTS + "] non empty parts separated by ':'";

    void validateDependency(String dependency) {
        String[] gavParts = dependency.split(":");
        if (gavParts.length != DEPENDENCY_PARTS ||
                StringUtils.isEmpty(gavParts[0].trim()) ||
                StringUtils.isEmpty(gavParts[1].trim()) ||
                StringUtils.isEmpty(gavParts[2].trim())) {
            throw new RuntimeException(INVALID_DEPENDENCY);
        }
    }
}
