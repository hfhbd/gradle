rootProject.name = "multiple-catalogs"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    // tag::multiple-catalogs[]
    versionCatalogs {
        create("tools") {
            from(files("gradle/tools.versions.toml"))
        }
    }
    // end::multiple-catalogs[]
}
