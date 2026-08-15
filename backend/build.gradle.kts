plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
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
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
