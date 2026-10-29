import info.preva1l.trashcan.paper
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    java
    id("fadah.publishing")
    id("info.preva1l.trashcan")
    id("com.gradleup.shadow")
}

repositories {
    // multilib est chargé au runtime par le libloader, qui retient le PREMIER dépôt où il le trouve : Clojars doit
    // passer avant libs/maven-repo, sinon libraries.json pointe vers ce dépôt file: du runner et le plugin ne charge pas.
    maven(url = "https://repo.clojars.org/") { content { includeGroup("com.github.puregero") } }
    maven { url = uri("$rootDir/libs/maven-repo") }
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    paper("26.2.build.134-stable")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
    testCompileOnly("org.jetbrains:annotations:26.0.2")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

configurations.all {
    resolutionStrategy.capabilitiesResolution.withCapability("org.bukkit:bukkit") {
        select("io.papermc.paper:paper-api:0")
    }
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        destinationDirectory.set(file("$rootDir/target"))
        archiveFileName.set("${rootProject.name}-${project.name.uppercaseFirstChar()}-$version.jar")
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
        options.release = 25
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    withType<Javadoc> {
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    register<Jar>("javadocJar") {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(named<Javadoc>("javadoc").get().destinationDir)
    }

    withType<Test> {
        useJUnitPlatform()
    }

    named("build") {
        dependsOn("shadowJar")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
        }
    }
}