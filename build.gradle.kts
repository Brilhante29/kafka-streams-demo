import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    application
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

group = "com.portfolio.streaming"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.portfolio.streaming.AppKt")
}

val integrationTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.21.4"))
    implementation("org.apache.kafka:kafka-streams:4.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(kotlin("test"))
    implementation("org.apache.kafka:kafka-streams-test-utils:4.3.1")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")

    add(integrationTest.implementationConfigurationName, "org.testcontainers:testcontainers-kafka:2.0.5")
    add(integrationTest.implementationConfigurationName, "org.assertj:assertj-core:3.27.7")
    add(integrationTest.implementationConfigurationName, "org.junit.jupiter:junit-jupiter:5.11.3")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.check {
    dependsOn("ktlintCheck")
}

tasks.register<Test>("integrationTest") {
    description = "Runs the broker-backed Kafka integration and evidence test."
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
    testLogging {
        events("passed", "skipped", "failed")
    }
}

dependencyLocking {
    lockAllConfigurations()
}
