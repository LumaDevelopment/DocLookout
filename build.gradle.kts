plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.joshuasheldon.doclookout"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "me.joshuasheldon.doclookout.DocLookout"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.joshuasheldon.doclookout.DocLookout"
    }
}