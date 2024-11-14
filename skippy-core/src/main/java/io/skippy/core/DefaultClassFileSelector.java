package io.skippy.core;

import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Default {@link ClassFileSelector} implementation.
 *
 * @author Florian McKee
 */
public final class DefaultClassFileSelector implements ClassFileSelector {

    /**
     * Comment to make the JavaDoc task happy.
     */
    public DefaultClassFileSelector() {
    }

    @Override
    public List<ClassFile> select(String className, List<ClassFile> candidates, List<String> classPath) {


        // no match? return nothing
        if (candidates.size() == 0) {
            return emptyList();
        }

        // exactly one match? return the match
        if (candidates.size() == 1) {
            return List.of(candidates.get(0));
        }

        // iterate through the classpath folders
        for (var classPathEntry : classPath) {

            // check for a match for the given folder
            var matchingClassFiles = candidates.stream().filter(classFile -> classFile.getOutputFolder().equals(Path.of(classPathEntry))).toList();

            if (matchingClassFiles.size() == 1) {
                return List.of(matchingClassFiles.get(0));
            }
        }

        // no exact match found: return all candidates (this is safe, but it will result in unnecessary execute predictions)
        return candidates;
    }
}
