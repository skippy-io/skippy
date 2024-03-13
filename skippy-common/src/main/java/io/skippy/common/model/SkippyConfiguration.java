package io.skippy.common.model;

public class SkippyConfiguration {

    private final boolean persistExecutionData;

    public SkippyConfiguration(boolean persistExecutionData) {
        this.persistExecutionData = persistExecutionData;
    }

    public boolean getPersistExecutionData() {
        return persistExecutionData;
    }

}