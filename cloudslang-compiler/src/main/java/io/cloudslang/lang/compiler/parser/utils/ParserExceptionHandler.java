/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.utils;

import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.scanner.ScannerException;

public class ParserExceptionHandler {

    public static final String CANNOT_CREATE_PROPERTY_ERROR = "Cannot create property";
    public static final String KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG =
            "Probably did not provide (key: value) pair or missing space after colon(:). " +
                    "Also check that everything is indented properly";
    public static final String MAPPING_VALUES_NOT_ALLOWED_HERE_ERROR = "mapping values are not allowed here";
    public static final String SCANNING_A_SIMPLE_KEY_ERROR = "while scanning a simple key";
    private static final String UNABLE_TO_FIND_PROPERTY_ERROR = "Unable to find property";
    private static final String MAP_CONSTRUCTOR_NOT_FOUND_ERROR =
            "No single argument constructor found for interface java.util.Map";
    private static final String TRUNCATION_BEGINNING = "Unable";
    private static final String TRUNCATION_END = "on class";

    public String getErrorMessage(Throwable e) {
        String errorMessage = e.getMessage();
        if (e instanceof ScannerException &&
                (errorMessage.startsWith(MAPPING_VALUES_NOT_ALLOWED_HERE_ERROR) ||
                        errorMessage.startsWith(SCANNING_A_SIMPLE_KEY_ERROR))) {
            errorMessage += KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG;
        } else if (e instanceof ConstructorException && errorMessage.startsWith(CANNOT_CREATE_PROPERTY_ERROR)) {
            if (errorMessage.contains(UNABLE_TO_FIND_PROPERTY_ERROR)) {
                //parse for undefined property name
                String truncatedErrorMessage = errorMessage.substring(errorMessage.indexOf(TRUNCATION_BEGINNING),
                        errorMessage.indexOf(TRUNCATION_END));
                String undefinedProperty = truncatedErrorMessage.substring(truncatedErrorMessage.indexOf("\'") + 1,
                        truncatedErrorMessage.lastIndexOf("\'"));
                errorMessage += "Property \'" + undefinedProperty + "\' is not supported by CloudSlang. Check that \'" +
                        undefinedProperty + "\' is indented properly.";

            } else if (errorMessage.contains(MAP_CONSTRUCTOR_NOT_FOUND_ERROR)) {
                errorMessage += KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG;
            }
        }
        return errorMessage;
    }
}
