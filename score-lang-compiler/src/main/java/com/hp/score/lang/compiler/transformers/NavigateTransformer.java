package com.hp.score.lang.compiler.transformers;
/*
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

/*
 * Created by orius123 on 05/11/14.
 */

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class NavigateTransformer implements Transformer<List<Map<String, String>>, LinkedHashMap<String, String>> {

    @Override
    public LinkedHashMap<String, String> transform(List<Map<String, String>> rawData) {
        LinkedHashMap<String, String> navigationData = new LinkedHashMap<>();
        //todo currently we support only string navigation
        for (Map<String, String> rawNavigation : rawData) {
            Map.Entry<String, String> entry = rawNavigation.entrySet().iterator().next();
            // - SUCCESS: some_task
            // the value of the navigation is the step to go to
            navigationData.put(entry.getKey(), entry.getValue());
        }
        return navigationData;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_TASK);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    public String keyToRegister() {
        return null;
    }
}

