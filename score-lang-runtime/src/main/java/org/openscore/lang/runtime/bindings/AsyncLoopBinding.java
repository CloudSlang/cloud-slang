/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.runtime.bindings;

import org.apache.commons.lang3.Validate;
import org.openscore.lang.entities.AsyncLoopStatement;
import org.openscore.lang.runtime.env.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 3/17/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class AsyncLoopBinding {

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public List<Serializable> bindAsyncLoopList(AsyncLoopStatement asyncLoopStatement, Context flowContext, String nodeName) {
        Validate.notNull(asyncLoopStatement, "async task statement cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(nodeName, "node name cannot be null");

        try {
            @SuppressWarnings("unchecked") List<Serializable> evalResult = (List<Serializable>) scriptEvaluator.evalExpr(
                    asyncLoopStatement.getExpression(),
                    flowContext.getImmutableViewOfVariables());
            return evalResult;
        } catch (Throwable t) {
            throw new RuntimeException("Error evaluating async task expression in task '" + nodeName + "', error is: \n" + t.getMessage(), t);
        }
    }
}
