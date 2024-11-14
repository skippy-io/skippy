package io.skippy.gradle.android;

import io.skippy.core.ClassFile;
import io.skippy.core.ClassFileSelector;
import io.skippy.core.DefaultClassFileSelector;

import java.util.List;

public class AndroidClassFileSelector implements ClassFileSelector {

    private static final DefaultClassFileSelector defaultSelector = new DefaultClassFileSelector();

    public AndroidClassFileSelector() {
    }

    @Override
    public List<ClassFile> select(String className, List<ClassFile> candidates, List<String> classPath) {
        // use default implementation for pre-filtration
        var filteredCandidates = defaultSelector.select(className, candidates, classPath);

        if (filteredCandidates.size() > 1) {
            // TODO: resolve candidates based on classpath
            return candidates;
        }
        return candidates;
    }
}
