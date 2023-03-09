import kotlinx.kover.api.IntellijEngine
import kotlinx.kover.api.JacocoEngine

val ktor_version: String by project // = 2.2.4
val kotlin_version: String by project // = "1.8.0"
val logback_version: String by project // = "1.2.11"
val datetime_version: String by project // = "0.4.0"
val exposed_version: String by project
val h2_version: String by project
val hikaricp_version: String by project


plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
    kotlin("plugin.serialization") version "1.8.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"

}

group = "com.kcchatapp"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetime_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    testImplementation("io.ktor:ktor-server-tests")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")}

kover {
    engine.set(IntellijEngine("1.0.683"))
//    engine.set(JacocoEngine("0.8.7"))

    verify {
        rule {
            name = "Minimal line coverage rate in percents"
            bound {
                minValue = 5
            }
        }
    }

    filters {
        classes {
            includes += listOf("com.kcchatapp.*")
            excludes += listOf("com.kcchatapp.db.*")
        }
    }
}
