// === PLUGINS =====================================================================================

plugins {
    java
    idea
}

// === MAIN BUILD DETAILS ==========================================================================

group = "com.norswap"
version = "1.0.2-ALPHA"
description = "Language implementation demo"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

val website = "https://github.com/norswap/${project.name}"
val vcs = "https://github.com/norswap/${project.name}.git"

sourceSets.main.get().java.srcDir("src")
sourceSets.test.get().java.srcDir("test")

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test.get().useTestNG()

tasks.javadoc.get().options {
    // https://github.com/gradle/gradle/issues/7038
    this as StandardJavadocDocletOptions
    addStringOption("Xdoclint:none", "-quiet")
    if (JavaVersion.current().isJava9Compatible)
        addBooleanOption("html5", true) // nice future proofing
}

tasks.withType(JavaCompile::class) {
    // Give unsafe casts details instead of passive agressively hinting that they exist.
    options.compilerArgs.plusAssign("-Xlint:unchecked")
    options.isDeprecation = true
}

// === IDE =========================================================================================

idea.module {
    // Download javadoc & sources for dependencies.
    isDownloadJavadoc = true
    isDownloadSources = true
}

// === DEPENDENCIES ================================================================================

repositories {
    mavenCentral()
    maven {
        url = uri("https://autumn.jfrog.io/artifactory/gradle")
    }
    mavenLocal()
}

dependencies {
    implementation("com.norswap:utils:2.1.12")
    implementation("com.norswap:autumn:1.2.0")
    implementation("com.norswap:uranium:1.0.11-ALPHA")
    implementation("org.ow2.asm:asm-all:5.2")
    testImplementation("org.testng:testng:7.5")
    testImplementation("org.slf4j:slf4j-simple:1.7.36")
}

// =================================================================================================