package io.cloudslang.lang.compiler.modeller.result;

import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public interface ModellingResult {

    List<RuntimeException> getErrors();
}
