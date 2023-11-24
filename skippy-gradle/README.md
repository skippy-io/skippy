# skippy-gradle-plugin

The Skippy plugin provides Skippy support in Gradle.

## Apply the Skippy Plugin

First, you have to apply the Skippy plugin:
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.skippy:skippy-gradle-plugin:0.0.4'
    }
}

apply plugin: io.skippy.gradle.SkippyPlugin
```
Note: The Skippy plugin automatically applies the `Java` plugin.

Releases and snapshots can be found at:
```
    ...
    repositories {
        // releases
        mavenCentral()
        
        // snapshots
        maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
    }
    ...
```


## Tasks

The plugin adds a couple of new tasks to your project:
```
./gradlew tasks
```
You will see the following output:
```
Skippy tasks
------------
skippyClean
skippyAnalysis
```

You will additionally see a bunch of internal tasks that you don't have to worry about too much: 
```
Skippy (internal) tasks
-----------------------
skippyCoverage_com.example.Class0Test
skippyCoverage_com.example.Class1Test
skippyCoverage_com.example.Class2Test
...
```

Those tasks capture execution data for individal tests, and are executed automatically when you run `skippyAnalysis`.

### skippyAnalysis Task

`skippyAnalysis` captures execution data for each test that uses Skippy. In addition, the plugin captures a hash of each source 
file:

```
./gradlew skippyAnalysis
```

You should see something like this:

```
./gradlew clean skippyAnalysis                                  

> Task skippyCoverage_com.example.Class0Test
Skippy: Capturing coverage data for com.example.Class0Test in skippy/com.example.Class0Test.csv

> Task skippyCoverage_com.example.Class1Test
Skippy: Capturing coverage data for com.example.Class1Test in skippy/com.example.Class1Test.csv

> Task skippyCoverage_com.example.Class2Test
Skippy: Capturing coverage data for com.example.Class2Test in skippy/com.example.Class2Test.csv

> Task skippyAnalysis
Skippy: Capturing a snapshot of all source files in skippy/sourceSnapshot.md5
```

The generated data is consumed by Skippy's testing libraries (e.g., 
[skippy-junit5](https://github.com/skippy-io/skippy-junit5)).

### skippyClean Task

`skippyClean` removes previously captured execution data and the file that contains a hash for every source file:

```
./gradlew skippyClean
```