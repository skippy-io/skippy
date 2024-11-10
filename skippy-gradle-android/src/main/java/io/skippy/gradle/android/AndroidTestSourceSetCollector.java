package io.skippy.gradle.android;

import com.android.build.api.dsl.AndroidSourceSet;

import java.io.File;
import java.util.stream.Stream;

public class AndroidTestSourceSetCollector {
    private AndroidTestSourceSetCollector() {}

    static Stream<File> collect(Stream<AndroidSourceSet> androidSourceSets) {
        return androidSourceSets
                .filter(androidSourceSet -> androidSourceSet.getName().startsWith("test") || androidSourceSet.getName().startsWith("androidTest"))
                .flatMap(AndroidTestSourceSetCollector::joinJavaAndKotlinSourceSets)
                .map(File::new);
    }

    static Stream<File> collectIfExists(Stream<AndroidSourceSet> androidSourceSets) {
        return collect(androidSourceSets)
                .filter(File::exists);
    }

    private static Stream<String> joinJavaAndKotlinSourceSets(AndroidSourceSet androidSourceSet) {
        var javaClassesDirs = androidSourceSet.getJava().getDirectories();
        var kotlinClassesDirs = androidSourceSet.getKotlin().getDirectories();

        return Stream.concat(javaClassesDirs.stream(), kotlinClassesDirs.stream());
    }
}
