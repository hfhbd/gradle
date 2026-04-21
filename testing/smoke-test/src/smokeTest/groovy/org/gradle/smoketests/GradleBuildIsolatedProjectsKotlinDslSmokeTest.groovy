/*
 * Copyright 2026 the original author or authors.
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

package org.gradle.smoketests

import org.gradle.integtests.fixtures.executer.IntegrationTestBuildContext
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.kotlin.dsl.KotlinDslScriptModel
import org.gradle.tooling.model.kotlin.dsl.KotlinDslScriptsModel
import org.gradle.util.internal.ToBeImplemented

class GradleBuildIsolatedProjectsKotlinDslSmokeTest extends AbstractGradleceptionSmokeTest {

    @ToBeImplemented
    def "KotlinDslScriptsModel for IP and non-IP mode are structurally equal"() {
        when:
        def runner = runner()
        def originalModel = fetchKotlinDslScriptsModel(runner)

        then:
        originalModel != null

        when:
        def isolatedModel = fetchKotlinDslScriptsModel(runner, '-Dorg.gradle.unsafe.isolated-projects=true')

        then:
        isolatedModel != null
        // TODO:isolated remove negation when reach models parity
        // See https://github.com/gradle/gradle/issues/37637
        !kotlinDslScriptsModelsAreEqual(isolatedModel, originalModel)
    }

    private KotlinDslScriptsModel fetchKotlinDslScriptsModel(SmokeTestGradleRunner runner, String... extraArgs) {
        try (ProjectConnection connection = GradleConnector.newConnector()
            .useGradleUserHomeDir(IntegrationTestBuildContext.INSTANCE.gradleUserHomeDir)
            .useInstallation(IntegrationTestBuildContext.INSTANCE.gradleHomeDir)
            .forProjectDirectory(testProjectDir)
            .connect()) {
            def modelBuilder = connection.model(KotlinDslScriptsModel)
                .addArguments(runner.arguments)
                .addJvmArguments(runner.jvmArguments)
                .setStandardOutput(System.out)
                .setStandardError(System.err)
            if (extraArgs) {
                modelBuilder.addArguments(extraArgs as List)
            }
            return modelBuilder.get()
        }
    }

    static boolean kotlinDslScriptsModelsAreEqual(KotlinDslScriptsModel actual, KotlinDslScriptsModel expected) {
        Map<File, KotlinDslScriptModel> actualModels = actual.scriptModels
        Map<File, KotlinDslScriptModel> expectedModels = expected.scriptModels
        if (actualModels.keySet() != expectedModels.keySet()) return false
        actualModels.every { File file, KotlinDslScriptModel actualScript ->
            KotlinDslScriptModel expectedScript = expectedModels[file]
            kotlinDslScriptModelsAreEqual(actualScript, expectedScript)
        }
    }

    static boolean kotlinDslScriptModelsAreEqual(KotlinDslScriptModel actual, KotlinDslScriptModel expected) {
        actual.classPath == expected.classPath &&
            actual.sourcePath == expected.sourcePath &&
            actual.implicitImports == expected.implicitImports &&
            // TODO:isolated support editor reports
            // actual.editorReports == expected.editorReports &&
            actual.exceptions == expected.exceptions
    }
}
