plugins {
    id("at.hugob.common") version "+"
    id("at.hugob.publish") version "+"
    id("at.hugob.shadow") version "+"
}

repositories {
    // paper-api
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // paper api
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // command stuff
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
    implementation("org.incendo:cloud-processors-confirmation:1.0.0-rc.1")
    // Confirmation Cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
}
