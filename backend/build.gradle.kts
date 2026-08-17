plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.gatling.gradle") version "3.15.1.2"
}

group = "com.conversor"
version = "1.0.0"
description = "Backend del conversor de monedas (Spring Boot + Neon + Frankfurter API)"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    // Web (REST controllers)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // WebClient para consumir Frankfurter API
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // JPA + Postgres (Neon)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // Validaciones
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Lombok 
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ------------------------------------------------------------
// Config de Gatling. El plugin crea el source set "gatling"
// automáticamente: src/gatling/java, src/gatling/resources.
// ------------------------------------------------------------
gatling {
    includeMainOutput = false
    includeTestOutput = false
}

// El plugin de Gatling compila su propio source set con un JVM
tasks.withType<JavaCompile> {
    options.release.set(21)
}
