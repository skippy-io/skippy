package io.skippy.gradle.android;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static java.util.Collections.emptySet;

import com.android.build.api.dsl.AndroidSourceDirectorySet;
import com.android.build.api.dsl.AndroidSourceSet;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Set;
import java.util.stream.Stream;

public class AndroidTestSourceSetCollectorTest {
    @Test
    void Given_list_of_sourceSets_with_empty_directories_When_AndroidTestSourceSetCollector_collect_Then_obtain_an_empty_result() {
        // Given
        var input = Stream.of(
                mockAndroidSourceSet("test", emptySet(), emptySet()),
                mockAndroidSourceSet("testSourceSet", emptySet(), emptySet()),
                mockAndroidSourceSet("irrelevantSourceSet", emptySet(), emptySet()),
                mockAndroidSourceSet("myGrandmaSourceSet", emptySet(), emptySet())
        );

        // When
        var actualResult = AndroidTestSourceSetCollector.collect(input);

        // Then
        assertTrue(actualResult.toList().isEmpty());
    }

    @Test
    void Given_list_of_sourceSets_When_AndroidTestSourceSetCollector_collect_Then_ignore_all_the_items_that_doesnt_contain_test_or_testAndroid_prefix() {
        // Given
        var input = Stream.of(
                mockAndroidSourceSet("test", Set.of("myJavaDirectory"), emptySet()),
                mockAndroidSourceSet("testSourceSet", Set.of("myJavaDirectory"), emptySet()),
                mockAndroidSourceSet("irrelevantSourceSet", Set.of("myJavaDirectory"), emptySet()),
                mockAndroidSourceSet("myGrandmaSourceSet", Set.of("myJavaDirectory"), emptySet())
        );

        // When
        var actualResult = AndroidTestSourceSetCollector.collect(input);

        // Then
        assertEquals(2, actualResult.toList().size());
    }

    @Test
    void Given_list_of_sourceSets_When_AndroidTestSourceSetCollector_collect_Then_all_java_and_kotlin_source_set_files_are_included() {
        // Given
        var testJavaDirectorySet = Set.of("myJavaDirectory1", "myJavaDirectory2", "myJavaDirectory3");
        var testKotlinDirectorySet = Set.of("myKotlinDirectory1", "myKotlinDirectory2", "myKotlinDirectory3");

        var input = Stream.of(
                mockAndroidSourceSet("test", testJavaDirectorySet, testKotlinDirectorySet),
                mockAndroidSourceSet("test2SourceSet", emptySet(), emptySet())
        );

        // When
        var actualResult = AndroidTestSourceSetCollector.collect(input).toList();

        // Then
        testJavaDirectorySet.stream().map(File::new);
        assertTrue(actualResult.containsAll(testJavaDirectorySet.stream().map(File::new).toList()));
        assertTrue(actualResult.containsAll(testKotlinDirectorySet.stream().map(File::new).toList()));
    }

    private AndroidSourceSet mockAndroidSourceSet(String givenName,
                                                  Set<String> javaDirectorySet,
                                                  Set<String> kotlinDirectorySet) {
        var androidSourceSetMock = Mockito.mock(AndroidSourceSet.class);
        given(androidSourceSetMock.getName()).willReturn(givenName);

        var javaAndroidSourceDirectorySet = Mockito.mock(AndroidSourceDirectorySet.class);
        given(javaAndroidSourceDirectorySet.getDirectories()).willReturn(javaDirectorySet);
        given(androidSourceSetMock.getJava()).willReturn(javaAndroidSourceDirectorySet);

        var kotlinAndroidSourceDirectorySet = Mockito.mock(AndroidSourceDirectorySet.class);
        given(kotlinAndroidSourceDirectorySet.getDirectories()).willReturn(kotlinDirectorySet);
        given(androidSourceSetMock.getKotlin()).willReturn(kotlinAndroidSourceDirectorySet);

        return androidSourceSetMock;
    }
}
