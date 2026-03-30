plugins {
    `java-library`
}

// tag::use-multiple-catalogs[]
dependencies {
    implementation(libs.guava)
    implementation(libs.commons.lang3)

    testImplementation(tools.junit.api)
    testImplementation(tools.mockito)
}
// end::use-multiple-catalogs[]
