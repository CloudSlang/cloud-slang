package com.hp.score.lang.compiler.utils;
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
import ch.lambdaj.Lambda;
import ch.lambdaj.function.matcher.Predicate;
import com.hp.score.lang.compiler.domain.SlangFile
;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static org.hamcrest.Matchers.not;

@Component
public class NamespaceBuilder {

    @Autowired
    private YamlParser yamlParser;

    public TreeMap<String, File> filterNonImportedFiles(TreeMap<String, File> namespaces, List<Map<String, String>> imports) {
        if (MapUtils.isEmpty(namespaces)) {
            throw new RuntimeException("no classpath but there are imports!!??");
        }
        TreeMap<String, File> importsFiles = new TreeMap<>();
        for (Map<String, String> anImport : imports) {
            Map.Entry<String, String> entry = anImport.entrySet().iterator().next();
            importsFiles.put(entry.getKey(), namespaces.get(entry.getValue().substring(0, entry.getValue().lastIndexOf("."))));
        }

        return importsFiles;
    }

    public List<File> filterClassPath(List<File> classpath) {
        String[] extensions = System.getProperty("classpath.extensions", "yaml,yml,py").split(",");
        List<File> filteredClassPath = new ArrayList<>();
        for (File file : classpath) {
            if (file.isDirectory()) {
                filteredClassPath.addAll(FileUtils.listFiles(file, extensions, true));
            } else {
                filteredClassPath.add(file);
            }
        }
        return filteredClassPath;
    }

    public TreeMap<String, File> sortByNameSpace(List<File> classpath) {
        TreeMap<String, File> namespaces = new TreeMap<>();
        final List<String> yamlExtensions = Arrays.asList("yaml", "yml");
        Predicate<File> isYaml = new Predicate<File>() {
            @Override
            public boolean apply(File file) {
                return yamlExtensions.contains(FilenameUtils.getExtension(file.getAbsolutePath()));
            }
        };
        Iterable<File> yamlFiles = Lambda.filter(isYaml, classpath);
        Iterable<File> otherFiles = Lambda.filter(not(isYaml), classpath);
        for (File yamlFile : yamlFiles) {
            SlangFile slangFile = yamlParser.loadMomaFile(yamlFile);
            namespaces.put(slangFile.getNamespace(), yamlFile);
        }
//        for (File file : otherFiles) {
//            namespaces.put(FilenameUtils.getBaseName(file.getAbsolutePath()), file);
//        }

        return namespaces;
    }


}
