pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "AliyunGradlePlugin"
			url = uri("https://maven.aliyun.com/repository/gradle-plugin")
		}
		maven {
			name = "AliyunPublic"
			url = uri("https://maven.aliyun.com/repository/public")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
	}
}

// Should match your modid
rootProject.name = "linearaim"
