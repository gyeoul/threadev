import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

fun getLocalProperty(key: String, file: String = "local.properties"): String? {
    val properties = Properties()
    val localProperties = File(file)
    if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties),Charsets.UTF_8).use {reader ->
            properties.load(reader)
        }
    } else error("File from not found")

    return properties.getProperty(key)
}

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("nu.studer.jooq") version "9.0"
}

group = "dev.gyeoul"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jooqGenerator(project(":jooq-custom"))
    jooqGenerator("org.jooq:jooq")
    jooqGenerator("org.jooq:jooq-meta")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDir(listOf("src/main/java","src/generated"))
        }
    }
}

val dbUser = getLocalProperty("db-user") ?: "postgres"
val dbPassword = getLocalProperty("db-pass") ?: "postgres"
val dbURL = getLocalProperty("db-url") ?: "jdbc:postgresql://localhost:5432/postgres"

jooq {
    //    version.set(jooqVersion)
    configurations {
        create("portal") {
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = dbURL
                    user = dbUser
                    password = dbPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "threadev"
                    }
                    generate.apply {
                        isDaos = true
                        isRecords = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                        isDeprecated = false
                        isValidationAnnotations = true
                        //                        isSpringAnnotations = true
                        //                        isSpringDao = true
                    }
                    target.apply {
                        directory = "src/generated"
                    }
                    strategy.name = "jooq.custom.generator.JPrefixGeneratorStrategy"
                }
            }
        }
    }
}
