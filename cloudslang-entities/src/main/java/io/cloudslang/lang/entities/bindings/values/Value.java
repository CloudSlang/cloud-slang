package io.cloudslang.lang.entities.bindings.values;

import java.io.Serializable;

/**
 * InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public interface Value extends Serializable {

    Serializable get();

    boolean isSensitive();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
