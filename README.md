# skippy

Mono-repo for Skippy projects:
- [skippy-core](skippy-core/README.md): Skippy functionality that is agnostic to build tools and testing frameworks.
- [skippy-gradle](skippy-gradle/README.md): Skippy's Test Impact Analysis for Gradle
- [skippy-junit5](skippy-junit5/README.md): Skippy's Conditional Test Execution for JUnit 5

## Sandbox Mode

You can set the content of the `sandbox` file temporarily to `true` to experiment with / debug the Skippy plugin using
the `skippy-gradle-sandbox` project without the need for build / publish cycles. Remember to undo your change before you 
commit your changes. Otherwise, `skippy-gradle` will not be deployed to Gradle's Plugin Portal or Maven Central.