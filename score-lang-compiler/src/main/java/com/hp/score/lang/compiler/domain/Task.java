package com.hp.score.lang.compiler.domain;/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

import java.io.Serializable;
import java.util.Map;

/*
 * Created by orius123 on 06/11/14.
 */
public class Task {

    private final String name;
    private final Map<String, Serializable> preTaskActionData;
    private final Map<String, Serializable> postTaskActionData;

    public Task(String name, Map<String, Serializable> preTaskActionData, Map<String, Serializable> postTaskActionData) {
        this.name = name;
        this.preTaskActionData = preTaskActionData;
        this.postTaskActionData = postTaskActionData;
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> getPreTaskActionData() {
        return preTaskActionData;
    }

    public Map<String, Serializable> getPostTaskActionData() {
        return postTaskActionData;
    }
}
