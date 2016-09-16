package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.MapForLoopStatement;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: bancl
 * Date: 8/12/2016
 */
public class AbstractBinding {
    protected void validateStringValue(String errorMessagePrefix, Value value) {
        if (value != null && value.get() != null && !(value.get() instanceof String)) {
            throw new RuntimeException(errorMessagePrefix + "' should have a String value.");
        }
    }

    protected Value getEvalResultForMap(Value evalResult, LoopStatement loopStatement, String collectionExpression) {
        if (loopStatement instanceof MapForLoopStatement) {
            if (evalResult != null && evalResult.get() instanceof Map) {
                List<Map.Entry<Value, Value>> entriesAsValues = new ArrayList<>();
                @SuppressWarnings("unchecked") Set<Map.Entry<Serializable, Serializable>> entrySet = ((Map) evalResult.get()).entrySet();
                for (Map.Entry<Serializable, Serializable> entry : entrySet) {
                    entriesAsValues.add(Pair.of(
                            ValueFactory.create(entry.getKey(), evalResult.isSensitive()),
                            ValueFactory.create(entry.getValue(), evalResult.isSensitive())));
                }
                evalResult = ValueFactory.create((Serializable) entriesAsValues);
            } else {
                throw new RuntimeException(LoopsBinding.INVALID_MAP_EXPRESSION_MESSAGE + ": " + collectionExpression);
            }
        }
        return evalResult;
    }
}
