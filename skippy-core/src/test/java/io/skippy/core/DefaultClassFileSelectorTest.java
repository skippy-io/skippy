package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultClassFileSelectorTest {

    ClassFileSelector selector = new DefaultClassFileSelector();

    ClassFileContainer classFileContainer = ClassFileContainer.parse(new Tokenizer(
                """
                    {
                        "0": {
                            "name": "com.example.Foo",
                            "path": "com/example/Foo.class",
                            "outputFolder": "src/main/java",
                            "hash": "00000000"
                        },
                        "1": {
                            "name": "com.example.Foo",
                            "path": "com/example/Foo.class",
                            "outputFolder": "src/main/kotlin",
                            "hash": "00000000"
                        },
                        "2": {
                            "name": "com.example.Foo",
                            "path": "com/example/Foo.class",
                            "outputFolder": "src/test/java",
                            "hash": "00000000"
                        },
                        "3": {
                            "name": "com.example.Bar",
                            "path": "com/example/Bar.class",
                            "outputFolder": "src/main/java",
                            "hash": "00000000"
                        },
                        "4": {
                            "name": "com.example.Bar",
                            "path": "com/example/Bar.class",
                            "outputFolder": "src/main/kotlin",
                            "hash": "00000000"
                        }
                    }
                """));

    @Test
    void testSelect() {
        assertEquals(asList(0, 1, 2), select("com.example.Foo", asList()));
        assertEquals(asList(3, 4), select("com.example.Bar", asList()));
        assertEquals(asList(), select("com.example.FooBar", asList()));
        assertEquals(asList(0), select("com.example.Foo", asList("src/main/java")));
        assertEquals(asList(0), select("com.example.Foo", asList("src/main/java", "src/main/kotlin")));
        assertEquals(asList(1), select("com.example.Foo", asList("src/main/kotlin", "src/main/java")));
        assertEquals(asList(2), select("com.example.Foo", asList("src/test/java", "src/main/java")));
        assertEquals(asList(0, 1, 2), select("com.example.Foo", asList("src/main/groovy")));
        assertEquals(asList(3, 4), select("com.example.Bar", asList("src/main/groovy")));
        assertEquals(asList(3), select("com.example.Bar", asList("src/main/java", "src/main/groovy")));
        assertEquals(asList(4), select("com.example.Bar", asList("src/main/groovy", "src/main/kotlin")));
    }

    private List<Integer> select(String className, List<String> classPath) {
        return selector.select(className, classFileContainer, classPath).stream().map(classFileContainer::getId).toList();
    }

}
