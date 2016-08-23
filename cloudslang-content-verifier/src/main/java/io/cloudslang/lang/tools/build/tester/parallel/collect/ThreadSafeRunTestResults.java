package io.cloudslang.lang.tools.build.tester.parallel.collect;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudslang.lang.tools.build.tester.TestRun;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThreadSafeRunTestResults {

    private ConcurrentMap<String, TestRun> passedTests;

    private ConcurrentMap<String, TestRun> failedTests;

    private ConcurrentMap<String, TestRun> skippedTests;

    private TreeSet<String> coveredExecutables;

    private TreeSet<String> uncoveredExecutables;

    private final Object lockPassedTests;
    private final Object lockFailedTests;
    private final Object lockSkippedTests;
    private final Object lockCoveredExecutables;
    private final Object lockUncoveredExecutables;

    public ThreadSafeRunTestResults() {
        this.passedTests = new ConcurrentHashMap<>();
        this.failedTests = new ConcurrentHashMap<>();
        this.skippedTests = new ConcurrentHashMap<>();
        coveredExecutables = new TreeSet<>();
        uncoveredExecutables = new TreeSet<>();

        lockPassedTests = new Object();
        lockFailedTests = new Object();
        lockSkippedTests = new Object();
        lockCoveredExecutables = new Object();
        lockUncoveredExecutables = new Object();





    }

    public Map<String, TestRun> getPassedTests() {
        synchronized (lockPassedTests) {
            return new HashMap<>(passedTests);
        }
    }

    public Map<String, TestRun> getFailedTests() {
        return failedTests;
    }

    public Map<String, TestRun> getSkippedTests() {
        return skippedTests;
    }

    public Set<String> getCoveredExecutables() {
        return coveredExecutables;
    }

    public Set<String> getUncoveredExecutables() {
        return uncoveredExecutables;
    }

    public void addPassedTest(String testCaseName, TestRun testRun){
        passedTests.put(testCaseName, testRun);
    }

    public void addFailedTest(String testCaseName, TestRun testRun){
        failedTests.put(testCaseName, testRun);
    }

    public void addSkippedTest(String testCaseName, TestRun testRun){
        skippedTests.put(testCaseName, testRun);
    }

    public synchronized void addCoveredExecutables(Set<String> coveredExecutables){
        this.coveredExecutables.addAll(coveredExecutables);
    }

    public synchronized void addUncoveredExecutables(Set<String> uncoveredExecutables){
        this.uncoveredExecutables.addAll(uncoveredExecutables);
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
        ThreadSafeRunTestResults rhs = (ThreadSafeRunTestResults) obj;
        return new EqualsBuilder()
                .append(this.passedTests, rhs.passedTests)
                .append(this.failedTests, rhs.failedTests)
                .append(this.skippedTests, rhs.skippedTests)
                .append(this.coveredExecutables, rhs.coveredExecutables)
                .append(this.uncoveredExecutables, rhs.uncoveredExecutables)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(passedTests)
                .append(failedTests)
                .append(skippedTests)
                .append(coveredExecutables)
                .append(uncoveredExecutables)
                .toHashCode();
    }
}
