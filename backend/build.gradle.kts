plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
}

group = "com.hmetsallik"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Web / REST API
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

	// Persistence — MyBatis version is pinned explicitly since it isn't part of Spring Boot's managed BOM
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1")
	runtimeOnly("org.postgresql:postgresql")

	// Flyway — Spring Boot 4.x requires the dedicated starter now, flyway-core alone isn't enough
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")

	// Boilerplate reduction (getters/setters/constructors)
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Bean Validation (@Valid, @NotNull, @Positive, etc. on request DTOs)
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// RabbitMQ event publishing
	implementation("org.springframework.boot:spring-boot-starter-amqp")

	// Required at test runtime for JUnit 5 platform discovery
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Integration tests — real Postgres + RabbitMQ in Docker (see AbstractIntegrationTest)
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-rabbitmq")

	// Integration tests — TestRestTemplate for full-stack HTTP round-trips.
	// Split into its own artifacts as of Spring Boot 4; both are required.
	testImplementation("org.springframework.boot:spring-boot-resttestclient")
	testImplementation("org.springframework.boot:spring-boot-starter-restclient")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.jacocoTestReport)
	violationRules {
		rule {
			limit {
				minimum = "0.80".toBigDecimal()
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}