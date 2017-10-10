package io.cloudslang.lang.compiler.validator.matcher;

import io.cloudslang.lang.entities.constants.Regex;

import java.util.regex.Pattern;

public class ExternalPathReferenceValidator extends PatternMatcher {
    public ExternalPathReferenceValidator() {
        super(Pattern.compile(Regex.EXTERNAL_PATH));
    }
}
