package io.cloudslang.lang.tools.build.tester.parallel.testcaseevents;

import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class FailedSlangTestCaseEvent extends SlangTestCaseEvent {

    private String failureReason;
    private Throwable failureException;

    public FailedSlangTestCaseEvent(SlangTestCase slangTestCase, String failureReason, Throwable failureException) {
        super(slangTestCase);
        this.failureReason = failureReason;
        this.failureException = failureException;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Throwable getFailureException() {
        return failureException;
    }

    public void setFailureException(Throwable failureException) {
        this.failureException = failureException;
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
        FailedSlangTestCaseEvent rhs = (FailedSlangTestCaseEvent) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.failureReason, rhs.failureReason)
                .append(this.failureException, rhs.failureException)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(failureReason)
                .append(failureException)
                .toHashCode();
    }
}
