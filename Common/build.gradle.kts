plugins {
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenCentral()

	maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
	api("org.spongepowered:configurate-hocon:4.1.0")
	api("org.spongepowered:configurate-extra-kotlin:4.1.0")

	api("co.aikar:acf-core:0.5.1-SNAPSHOT")

	api("org.jetbrains.kotlin:kotlin-reflect:1.7.10")

	api("org.jetbrains.exposed:exposed-core:0.39.2")
	api("org.jetbrains.exposed:exposed-jdbc:0.39.2")
	api("org.jetbrains.exposed:exposed-dao:0.39.2")

	api("org.xerial:sqlite-jdbc:3.39.3.0")
	api("mysql:mysql-connector-java:8.0.30")
}