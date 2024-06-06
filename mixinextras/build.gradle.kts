import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

allprojects {
    apply(plugin = "java")

    group = "com.llamalad7"
    version = "0.3.6"

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(project(":"))
        compileOnly("org.ow2.asm:asm-debug-all:5.2")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<Javadoc> {
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

val shade by configurations.creating {
    configurations.compileOnly.get().extendsFrom(this)
}

val shadeOnly by configurations.creating

dependencies {
    shade("org.apache.commons:commons-lang3:3.3.2")
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shade, shadeOnly)
    archiveClassifier = "fat"
    relocate("org.apache.commons.lang3", "com.llamalad7.mixinextras.lib.apache.commons")
    exclude("META-INF/maven/**/*", "META-INF/*.txt")
    from("LICENSE") {
        rename { "${it}_MixinExtras"}
    }
}

tasks.named<Jar>("jar") {
    archiveClassifier = "slim"
}