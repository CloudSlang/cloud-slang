package io.cloudslang.lang.tools.build.tester.parallel.testcaseevents;

import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class BeginSlangTestCaseEvent extends SlangTestCaseEvent {

    public BeginSlangTestCaseEvent(SlangTestCase slangTestCase) {
        super(slangTestCase);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        BeginSlangTestCaseEvent rhs = (BeginSlangTestCaseEvent) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
