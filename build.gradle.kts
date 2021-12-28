plugins {
    java
}

group = "net.techcable"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    /*
     * Jetbrains annotations is a *compile-only* dependency.
     *
     * It provides us with @NonNull and @Nullable
     */
    compileOnly("org.jetbrains:annotations:20.1.0")
    /*
     * For testing, we need to use
     */
    // testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}