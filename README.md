![Skippy](https://avatars.githubusercontent.com/u/150977247?s=100&u=6f4eb4ad99fb667b1bfaf988d3d396bd892fdf16&v=4)

# skippy

Mono-repo for all Skippy projects.

## What is it?

Skippy is a Test Impact Analysis & Predictive Test Selection framework for the JVM. It cuts down on unnecessary testing
and flakiness without compromising the integrity of your builds. You can run it from the command line, your favorite IDE
and continuous integration server. Skippy supports Gradle, Maven, JUnit 4 and JUnit 5.

Skippy is specifically designed to prevent regressions in your codebase.
It supports all types of tests where the tests and the code under test run in the same JVM.
It is best suited for deterministic tests, even those prone to occasional flakiness.
It provides the most value for test suites that are either slow or flaky (regardless of whether the test suite contains unit, integration, or functional tests).

## What is it not?

Skippy is not designed for tests that assert the overall health of a system. Don't use it for tests you want to fail
in response to misbehaving services, infrastructure issues, etc.

## Highlights

- Support for Gradle & Maven
- Support for JUnit 4 & JUnit 5
- Lightweight: Use it from the command line, your favorite IDE and CI server
- Non-invasive: Use it for a single test, your entire suite and anything in-between
- Free of lock-in: You can go back to a "run everything" approach at any time
- Open Source under Apache 2 License

## Getting Started

The best way to get started are the introductory tutorials for Gradle and Maven:
- [Getting Started with Skippy, Gradle & JUnit 5](https://www.skippy.io/tutorials/skippy-gradle-junit5)
- [Getting Started with Skippy, Maven & JUnit 5](https://www.skippy.io/tutorials/skippy-maven-junit5)

From there, good next steps are:
- [How Skippy Works - A Deep Dive](https://www.skippy.io/tutorials/how-skippy-works)
- [Reference Documentation](https://www.skippy.io/docs)

## Teaser

Let's take a whirlwind tour of Skippy, Gradle & JUnit 5. The concepts are similar for Maven & JUnit 4.

### Step 1: Install Skippy

```groovy
    plugins {
+       id 'io.skippy' version '0.0.14'
    }
    
    dependencies {
+       testImplementation 'io.skippy:skippy-junit5:0.0.14'
    }
```

### Step 2: Skippify Your Tests

```java
+    import io.skippy.junit5.Skippified;

+    @Skippified
     public class FooTest {     

         @Test
         public void testGetFoo() {
             assertEquals("foo", Foo.getFoo());
         }

     }
```

### Step 3: Perform a Test Impact Analysis

```
./gradlew skippyAnalyze
```

`skippyAnalyze` stores impact data for skippified tests and a bunch of other files in the .skippy folder.

```
ls -l .skippy

classes.md5
com.example.FooTest.cov
...
```

### Step 4: Skippy's Predictive Test Selection In Action

```
./gradlew test

FooTest > testFoo() SKIPPED
BarTest > testBar() SKIPPED
```

Skippy compares the current state of the project with the data in the .skippy folder to make skip-or-execute
predictions for skippified tests. Skippy detects no changes and skips `FooTest` and `BarTest`.

Introduce a bug in class `Foo`:
```java
     class Foo {
    
         static String getFoo() {
-            return "foo";
+            return "null";
         }
         
     }

```

Re-run the tests:

```
./gradlew test

FooTest > testFoo() FAILED
BarTest > testBar() SKIPPED
```

Skippy detects the change and makes an execute prediction for `FooTest`. The regression is caught quickly -
unrelated tests remain skipped.

## Use Skippy In Your CI Pipeline

It's safe to add the .skippy folder to version control. This will automatically enable Skippy's Predictive Test
Selection when your pipeline runs. Support to store impact data outside the filesystem (e.g., a
database) is on the roadmap: https://github.com/skippy-io/skippy/issues/104

## Contributions & Issues

Contributions are always welcome! You can either
- submit a pull request,
- create an issue in
  [GitHub's issue tracker](https://github.com/skippy-io/skippy/issues) or
- email [contact@skippy.io](mailto:contact@skippy.io).

I would love to hear from you.

## Building Skippy Locally

You need JDK 17 or upwards to build Skippy.

If you want to run the entire build including tests, use `build`:

```
./gradlew build
```

If you want to publish all jars to your local Maven repository, use `publishToMavenLocal`:

```
./gradlew publishToMavenLocal
```

## Projects in this repo

This repo contains the following sub-projects:

- [skippy-gradle](skippy-gradle/README.md): Skippy's Test Impact Analysis for Gradle
- [skippy-maven](skippy-maven/README.md): Skippy's Test Impact Analysis for Maven
- [skippy-junit4](skippy-junit4/README.md): Skippy's Predictive Test Selection For JUnit 4
- [skippy-junit5](skippy-junit5/README.md): Skippy's Predictive Test Selection For JUnit 5
- [skippy-common](skippy-common/README.md): Common functionality for all libraries in this repo
- [skippy-build-common](skippy-build-common/README.md): Common functionality for `skippy-gradle` and `skippy-maven`
- [skippy-junit-common](skippy-junit-common/README.md): Common functionality for `skippy-junit4` and `skippy-junit5`