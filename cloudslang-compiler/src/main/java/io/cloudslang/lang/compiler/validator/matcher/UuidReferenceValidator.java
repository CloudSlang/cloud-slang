package io.cloudslang.lang.compiler.validator.matcher;

import io.cloudslang.lang.entities.constants.Regex;

import java.util.regex.Pattern;

public class UuidReferenceValidator extends PatternMatcher {
    public UuidReferenceValidator() {
        super(Pattern.compile(Regex.UUID));
    }
}
