package io.cloudslang.lang.cli.services;

import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.score.events.ScoreEventListener;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @version $Id$
 * @since 12/09/2014
 */
public interface ScoreServices {
    void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);

    Long trigger(CompilationArtifact compilationArtifact, Map<String, Value> inputs, Set<SystemProperty> systemProperties);

    Long triggerSync(CompilationArtifact compilationArtifact, Map<String, Value> inputs, Set<SystemProperty> systemProperties, boolean quiet, boolean debug);
}
