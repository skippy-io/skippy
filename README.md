# skippy

Mono-repo for Skippy projects:
- [skippy-core](skippy-core/README.md): Functionality that is agnostic to build and testing tools
- [skippy-gradle](skippy-gradle/README.md): Skippy's test analysis for Gradle
- [skippy-maven](skippy-maven/README.md): Skippy's test analysis for Maven
- [skippy-junit4](skippy-junit4/README.md): Skippy's conditional test execution For JUnit 4
- [skippy-junit5](skippy-junit5/README.md): Skippy's conditional test execution For JUnit 5

## Sandbox Mode

You can set the content of the `sandbox` file temporarily to `true` to experiment with / debug the Skippy plugin using
the `skippy-gradle-sandbox` project without the need for build / publish cycles. Remember to undo your change before you 
commit your changes. Otherwise, `skippy-gradle` will not be deployed to Gradle's Plugin Portal or Maven Central.