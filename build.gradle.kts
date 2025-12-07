plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
}

group = "ture"
version = "0.0.1"
description = "hw2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Log4j2
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    // Для structured logging
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl")

    // Для JSON логов
    implementation("com.fasterxml.jackson.core:jackson-databind")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    implementation("org.springframework.boot:spring-boot-starter-graphql")


    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // === gRPC зависимости ===
    implementation("net.devh:grpc-spring-boot-starter:2.15.0.RELEASE")

    implementation("io.grpc:grpc-netty:1.62.2")
    implementation("io.grpc:grpc-protobuf:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("com.google.protobuf:protobuf-java-util:3.25.3")

    // Для работы с рефлексией (нужно для grpcurl и тестирования)
    implementation("io.grpc:grpc-services:1.62.2") // reflection service

    compileOnly("org.apache.tomcat:annotations-api:6.0.53") // для @Generated
}

// === Конфигурация protobuf плагина ===
protobuf {
    protoc {
        // Указываем путь к protoc
        artifact = "com.google.protobuf:protoc:3.25.3"
    }

    plugins {
        // Правильный синтаксис для Kotlin DSL
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.62.2"
        }
    }

    generateProtoTasks {
        // Для всех задач генерации
        all().forEach { task ->
            // Добавляем плагин grpc
            task.plugins {
                create("grpc")
            }
        }
    }
}

// === Настройка sourceSets ===
sourceSets {
    main {
        java {
            srcDirs(
                "src/main/java",
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc"
            )
        }
    }
}

// === Очистка сгенерированных файлов ===
tasks {
    clean {
        delete.add("build/generated")
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

