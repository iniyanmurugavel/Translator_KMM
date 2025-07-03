plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.native.cocoapods) apply false
    alias(libs.plugins.ksp) apply false
}

// Clean task to remove build directories and metadata
tasks.register("clean", Delete::class) {
    group = "build"
    description = "Clean build directories and Kotlin metadata"

    delete(rootProject.layout.buildDirectory)

    // Clean Kotlin metadata from root
    doLast {
        val kotlinMetadataDir = rootProject.layout.projectDirectory.dir(".kotlin")
        val jsStoreDir = rootProject.layout.projectDirectory.dir("kotlin-js-store")

        if (kotlinMetadataDir.asFile.exists()) {
            delete(kotlinMetadataDir)
            println("✓ Cleaned .kotlin metadata directory")
        }

        if (jsStoreDir.asFile.exists()) {
            delete(jsStoreDir)
            println("✓ Cleaned kotlin-js-store directory")
        }
    }
}

// Deep clean task for thorough cleanup
tasks.register("cleanAll") {
    group = "build"
    description = "Deep clean: build directories, caches, metadata, and derived data"

    dependsOn("clean")

    doLast {
        val dirsToClean = listOf(
            ".gradle",
            ".kotlin",
            "kotlin-js-store",
            "build"
        )

        dirsToClean.forEach { dirName ->
            val dir = rootProject.layout.projectDirectory.dir(dirName)
            if (dir.asFile.exists()) {
                delete(dir)
                println("✓ Deleted $dirName")
            }
        }

        // Clean subproject build and metadata directories
        subprojects.forEach { subproject ->
            val buildDir = subproject.layout.buildDirectory.asFile.get()
            val kotlinDir = subproject.layout.projectDirectory.dir(".kotlin").asFile
            val jsStoreDir = subproject.layout.projectDirectory.dir("kotlin-js-store").asFile

            if (buildDir.exists()) {
                delete(buildDir)
                println("✓ Cleaned ${subproject.name}/build")
            }

            if (kotlinDir.exists()) {
                delete(kotlinDir)
                println("✓ Cleaned ${subproject.name}/.kotlin")
            }

            if (jsStoreDir.exists()) {
                delete(jsStoreDir)
                println("✓ Cleaned ${subproject.name}/kotlin-js-store")
            }
        }

        println("🧹 Deep clean completed!")
    }
}

// Task to clean only Kotlin metadata (lightweight)
tasks.register("cleanMetadata") {
    group = "build"
    description = "Clean only Kotlin metadata files"

    doLast {
        // Clean metadata from root and all subprojects
        val allProjects = listOf(rootProject) + subprojects

        allProjects.forEach { project ->
            val kotlinDir = project.layout.projectDirectory.dir(".kotlin").asFile
            val jsStoreDir = project.layout.projectDirectory.dir("kotlin-js-store").asFile

            if (kotlinDir.exists()) {
                delete(kotlinDir)
                println("✓ Cleaned ${project.name}/.kotlin")
            }

            if (jsStoreDir.exists()) {
                delete(jsStoreDir)
                println("✓ Cleaned ${project.name}/kotlin-js-store")
            }
        }

        println("🗂️ Metadata cleanup completed!")
    }
}

// Configure all subprojects
subprojects {
    // Ensure clean task exists and depends on metadata cleanup
    afterEvaluate {
        tasks.findByName("clean")?.apply {
            doLast {
                val kotlinDir = layout.projectDirectory.dir(".kotlin").asFile
                val jsStoreDir = layout.projectDirectory.dir("kotlin-js-store").asFile

                if (kotlinDir.exists()) {
                    delete(kotlinDir)
                }

                if (jsStoreDir.exists()) {
                    delete(jsStoreDir)
                }
            }
        }
    }
}