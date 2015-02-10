/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.runtime.env;

import org.python.core.PyObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ForLoopCondition implements LoopCondition {

    private final Iterator<? extends Serializable> iterator;

    public ForLoopCondition(Iterator<? extends Serializable> iterator) {
        this.iterator = iterator;
    }

    public static ForLoopCondition create(Serializable serializable, String collectionExpression, String nodeName){
        Iterator<? extends Serializable> iterator;

        if (serializable instanceof Iterable) {
            Iterable<Serializable> serializableIterable = (Iterable<Serializable>) serializable;
            iterator = serializableIterable.iterator();
        } else if (serializable instanceof String) {
            String[] strings = ((String) serializable).split(Pattern.quote(","));
            List<String> list = Arrays.asList(strings);
            iterator = list.iterator();
        } else if (serializable instanceof PyObject) {
            PyObject pyObject = (PyObject) serializable;
            iterator = pyObject.asIterable().iterator();
        } else {
            throw new RuntimeException("collection expression: '" + collectionExpression + "' in the 'for' loop " +
                    "in task: '" + nodeName + "' " +
                    "doesn't return an iterable, other types are not supported");
        }

        return new ForLoopCondition(iterator);
    }

    public Serializable next() {
        return iterator.next();
    }

    @Override
    public boolean hasMore() {
        return iterator.hasNext();
    }
}
