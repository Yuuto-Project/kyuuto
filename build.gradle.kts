/*
 * Open source bot built by and for the Camp Buddy Discord Fan Server.
 *     Copyright (C) 2020  Kyuuto-devs
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

plugins {
    idea
    application
    kotlin("jvm") version "1.3.72"
    id("org.jmailen.kotlinter") version "2.3.2"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

project.group = "io.github.yuutoproject"
project.version = "4.0-alpha"

repositories {
    jcenter()
}

// JDA and logback-classic are written in java
// But kotlin has terrific java interop
dependencies {
    // Kotlin STD and other kotlin stuff
    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.5")
    // The discord lib
    implementation(group = "net.dv8tion", name = "JDA", version = "4.1.1_152") {
        exclude(module = "opus-java")
    }
    // dotenv support
    implementation(group = "io.github.cdimascio", name = "java-dotenv", version = "5.1.3")
    // For logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    // For loading the commands (super small lib)
    implementation(group = "org.reflections", name = "reflections", version = "0.9.12")
    // Http client
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.5.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    wrapper {
        gradleVersion = "6.3"
        distributionType = DistributionType.ALL
    }
    shadowJar {
        archiveClassifier.set("shadow")

        manifest {
            attributes["Description"] = "Kyuuto is cute tho"
        }
    }
}

// Force a minimum of java 11
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "${project.group}.yuutobot.YuutoKt"
}

kotlinter {
    ignoreFailures = false
    indentSize = 4
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = false
    disabledRules = arrayOf("no-wildcard-imports")
    fileBatchSize = 30
}
