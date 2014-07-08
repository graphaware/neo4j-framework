package com.graphaware.example;

import com.graphaware.test.performance.PerformanceTest;
import com.graphaware.test.performance.PerformanceTestSuite;

/**
 * Dummy {@link PerformanceTestSuite} for documentation. Runs {@link DummyTestForDocs}.
 */
public class DummyTestSuiteForDocs extends PerformanceTestSuite {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PerformanceTest[] getPerfTests() {
        return new PerformanceTest[]{
                new DummyTestForDocs()
        };
    }
}
