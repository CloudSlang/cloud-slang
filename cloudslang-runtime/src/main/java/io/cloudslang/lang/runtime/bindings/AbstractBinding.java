/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapLoopStatement;
import io.cloudslang.lang.entities.bindings.prompt.Prompt;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.utils.ValidationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.python.core.PyObject;
import org.springframework.beans.factory.annotation.Autowired;

import static io.cloudslang.lang.entities.utils.ExpressionUtils.extractExpression;

public class AbstractBinding {

    private static final String SENSITIVE_VALUE_IN_PROMPT_OPTION_ERROR =
            "Sensitive values can't be used as an option for selection like fields.";

    @Autowired
    protected ScriptEvaluator scriptEvaluator;

    protected void validateStringValue(String errorMessagePrefix, Value value) {
        if (value != null) {
            ValidationUtils.validateStringValue(errorMessagePrefix, value.get());
        }
    }

    protected Value getEvalResultForMap(Value evalResult, LoopStatement loopStatement, String collectionExpression) {
        if (loopStatement instanceof MapLoopStatement) {
            if (evalResult != null && evalResult.get() instanceof Map) {
                //noinspection unchecked
                Set<Map.Entry<Serializable, Serializable>> entrySet =
                        ((Map) evalResult.get()).entrySet();

                List<Value> entriesAsValues = new ArrayList<>();
                for (Map.Entry<Serializable, Serializable> entry : entrySet) {
                    entriesAsValues.add(ValueFactory.create(Pair.of(
                            ValueFactory.create(entry.getKey(), evalResult.isSensitive()),
                            ValueFactory.create(entry.getValue(), evalResult.isSensitive()))));
                }
                evalResult = ValueFactory.create((Serializable) entriesAsValues);
            } else {
                throw new RuntimeException(LoopsBinding.INVALID_MAP_EXPRESSION_MESSAGE + ": " + collectionExpression);
            }
        }
        return evalResult;
    }

    protected Iterable<Value> getIterableFromEvalResult(Value loopCollection) {
        Serializable loopCollectionContent = loopCollection.get();
        if (loopCollectionContent instanceof Iterable) {
            //noinspection unchecked
            Iterable<? extends Serializable> loopCollectionContentSerializable =
                    (Iterable<? extends Serializable>) loopCollectionContent;
            return convert(loopCollectionContentSerializable, loopCollection.isSensitive());
        } else if (loopCollectionContent instanceof String) {
            String expression = (String) loopCollectionContent;
            if ((expression.length() >= 2) &&
                    (expression.charAt(0) == '{') && (expression.charAt(expression.length() - 1) == '}')) {
                expression = expression.substring(1, expression.length() - 1);
                expression = expression.replace("\"", "");
                ArrayList<String> keys = new ArrayList<>();
                for (String value : expression.split(",")) {
                    keys.add(value.split(":")[0]);
                }
                return convert(keys, loopCollection.isSensitive());
            } else {
                String[] strings = ((String) loopCollectionContent).split(Pattern.quote(","));
                return convert(Arrays.asList(strings), loopCollection.isSensitive());
            }
        } else if (loopCollectionContent instanceof PyObject) {
            PyObject pyObject = (PyObject) loopCollectionContent;
            return convert(pyObject.asIterable(), loopCollection.isSensitive());
        } else {
            return null;
        }
    }

    private Iterable<Value> convert(Iterable<? extends Serializable> iterable, boolean sensitive) {
        List<Value> values = new ArrayList<>();
        for (Serializable serializable : iterable) {
            values.add(ValueFactory.create(serializable, sensitive));
        }
        return values;
    }

    protected Optional<Value> tryEvaluateExpression(Serializable expression,
                                                    EvaluationContextHolder evaluationContextHolder) {
        String expressionToEvaluate = extractExpression(expression);

        if (expressionToEvaluate != null) {
            Map<String, Value> evaluationContext = evaluationContextHolder.createEvaluationContext();

            return Optional.of(scriptEvaluator.evalExpr(expressionToEvaluate,
                    evaluationContext,
                    evaluationContextHolder.getSystemProperties(),
                    evaluationContextHolder.getFunctionDependencies()));
        } else {
            return Optional.empty();
        }
    }

    protected void resolvePromptExpressions(Prompt prompt, EvaluationContextHolder evaluationContextHolder) {
        // prompt message
        tryEvaluateExpression(prompt.getPromptMessage(), evaluationContextHolder)
                .map(Value::toStringSafeEmpty)
                .ifPresent(prompt::setPromptMessage);

        // in case of single/multi-choice
        if (prompt.getPromptType().isChoiceLike()) {
            // prompt options
            tryEvaluateExpression(prompt.getPromptOptions(), evaluationContextHolder)
                    .ifPresent(value -> {
                        if (value.isSensitive()) {
                            throw new RuntimeException(SENSITIVE_VALUE_IN_PROMPT_OPTION_ERROR);
                        }
                        prompt.setPromptOptions(Value.toStringSafeEmpty(value));
                    });

            // prompt delimiter
            tryEvaluateExpression(prompt.getPromptDelimiter(), evaluationContextHolder)
                    .map(Value::toStringSafeEmpty)
                    .ifPresent(prompt::setPromptDelimiter);
        }
    }

}
