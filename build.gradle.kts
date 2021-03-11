import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    maven
}

group = "ok.work"
version = "0.1.3"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.lightstreamer.com/repo/maven")
    }

}

dependencies {
    compile("org.springframework.boot:spring-boot-starter")
    compile("org.springframework.boot:spring-boot-starter-web")

    compile("org.springframework.boot:spring-boot-starter-websocket")

    compile("org.json:json:20180813")

    compile("org.jetbrains.kotlin:kotlin-reflect")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    compile("com.lightstreamer:ls-javase-client:3.1.1")

    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")

    compile("org.slf4j:slf4j-api:1.7.30")
    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("ch.qos.logback:logback-core:1.2.3")
    compile("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    compile("ch.qos.logback.contrib:logback-jackson:0.1.5")
    compile("javax.mail:mail:1.4.7")
    compile("javax.activation:activation:1.1.1")

    // Swagger
    compile("io.springfox:springfox-swagger2:2.9.2")
    compile("io.springfox:springfox-swagger-ui:2.9.2")
    compile("io.springfox:springfox-swagger-common:2.9.2")

    compile("org.seleniumhq.selenium:selenium-java:3.0.1")
    compile("com.github.detro.ghostdriver:phantomjsdriver:1.0.1")

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    compile("com.squareup.okhttp3:okhttp:4.7.2")

    testCompile("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

