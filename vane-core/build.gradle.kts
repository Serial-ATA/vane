plugins {
	id("com.github.johnrengelman.shadow") version "7.1.0"
}

dependencies {
	implementation(group = "org.bstats", name = "bstats-bukkit", version = "1.8")
	implementation(group = "org.reflections", name = "reflections", version = "0.9.12")
	implementation(group = "org.json", name = "json", version = "20200518")
	implementation(project(":vane-annotations"))
}

tasks {
	shadowJar {
		dependencies {
			include(dependency("org.bstats:bstats-bukkit"))
			include(dependency("org.reflections:reflections"))
			include(dependency("org.json:json"))
			include(dependency(":vane-annotations"))
		}
		relocate("org.bstats", "org.oddlama.vane.external.bstats")
		relocate("org.reflections", "org.oddlama.vane.external.reflections")
		relocate("org.json", "org.oddlama.vane.external.json")
	}

	copyJar {
		from("shadowJar")
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
		rename("(.*)-all.jar", "$1.jar")
	}
}