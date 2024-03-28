/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime;

/**
 * Date: 4/16/2015
 *
 * @author Bonczidai Levente
 */
public interface RuntimeConstants {

    // parallel loop
    String BRANCHES_CONTEXT_KEY = "branches_context";
    String SPLIT_ITEM_KEY = "splitItem";
    String BRANCH_RETURN_VALUES_KEY = "branchReturnValues";

    String EXECUTION_ID = "run_id";
    String USER_ID = "get_user_id()";
    String WORKER_GROUP = "get_worker_group()";
    String RUN_ID = "get_run_id()";

}
