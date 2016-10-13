package io.cloudslang.lang.commons.services.api;

import io.cloudslang.lang.entities.bindings.values.Value;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 8/23/2016
 */
public interface SlangSourceService {
    Map<String, Value> convertInputFromMap(Map<String, ? extends Serializable> rawMap, String artifact);
}
