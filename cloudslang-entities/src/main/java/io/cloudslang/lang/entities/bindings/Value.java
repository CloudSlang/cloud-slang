package io.cloudslang.lang.entities.bindings;

import java.io.Serializable;

/**
 * InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public interface Value extends Serializable {

    @Override
    String toString();

    Serializable get();
}
