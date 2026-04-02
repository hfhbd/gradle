/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl.plugins.dsl

import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.gradle.kotlin.dsl.fixtures.AbstractKotlinIntegrationTest
import org.gradle.util.internal.VersionNumber
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Tests that the `kotlin-dsl` plugin can be applied alongside an explicit, different version
 * of the `org.jetbrains.kotlin.jvm` plugin.
 */
@RunWith(Parameterized::class)
class KotlinDslPluginWithExplicitKGPVersionTest(private val kotlinVersionString: String) : AbstractKotlinIntegrationTest() {

    companion object {

        private const val KOTLIN_STDLIB_MAVEN_METADATA_URL =
            "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/maven-metadata.xml"

        private fun fetchKotlinVersionsFromMavenCentral(): List<Pair<String, VersionNumber>> {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(KOTLIN_STDLIB_MAVEN_METADATA_URL)
            val versions = doc.getElementsByTagName("version")
            return (0 until versions.length)
                .map { versions.item(it).textContent }
                .map { it to VersionNumber.parse(it) }
                .sortedBy { it.second }
        }

        @Parameterized.Parameters(name = "KGP {0}")
        @JvmStatic
        fun testedKotlinVersions(): List<String> {
            val embeddedVersion = VersionNumber.parse(embeddedKotlinVersion)
            val allVersions = fetchKotlinVersionsFromMavenCentral()
            val stableVersions = allVersions.filter { it.second.baseVersion == it.second }

            // Latest available version from Maven Central that is newer than embedded
            val latest = allVersions
                .lastOrNull { it.second > embeddedVersion }
                ?.first

            // Latest stable version older than the embedded version (always differs)
            val latestOlderStable = stableVersions
                .lastOrNull { it.second < embeddedVersion }
                ?.first

            return listOfNotNull(latestOlderStable, latest)
        }
    }

    @Test
    fun `can apply kotlin-dsl plugin with explicit different kotlin-jvm plugin version`() {

        withBuildScript(
            """

            plugins {
                `kotlin-dsl`
                id("org.jetbrains.kotlin.jvm") version "$kotlinVersionString"
            }

            $repositoriesBlock

            """
        )

        withFile(
            "src/main/kotlin/code.kt",
            """

            import org.gradle.api.Plugin
            import org.gradle.api.Project

            class MyPlugin : Plugin<Project> {
                override fun apply(project: Project) {
                    println("applied!")
                }
            }

            """
        )

        build("classes")
    }
}
