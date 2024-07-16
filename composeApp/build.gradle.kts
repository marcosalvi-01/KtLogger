import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	alias(libs.plugins.kotlinMultiplatform)

	alias(libs.plugins.jetbrainsCompose)
	kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
	jvm("desktop")

	sourceSets {
		val desktopMain by getting

		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material)
			implementation(compose.ui)
			implementation(compose.components.resources)
			implementation(compose.components.uiToolingPreview)
		}
		desktopMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation("net.java.dev.jna:jna:5.14.0")
			implementation("net.java.dev.jna:jna-platform:5.14.0")
			implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
			implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0")
			implementation("com.darkrockstudios:mpfilepicker:3.1.0")
			implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

			val exposedVersion = "0.45.0"
			implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
			implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
			implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
			implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
			implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")

			implementation("org.xerial:sqlite-jdbc:3.45.2.0")
			implementation("ch.qos.logback:logback-classic:1.2.9")

			val voyagerVersion = "1.0.0"
			implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
			implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
			implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
			implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
			implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
		}
	}
}


compose.desktop {
	application {
		mainClass = "MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

			windows {
				iconFile.set(project.file("resources/app_icon.ico"))
			}

			packageName = "KtLogger"
			packageVersion = "1.0.0"
			modules("java.sql")
		}
	}
}
