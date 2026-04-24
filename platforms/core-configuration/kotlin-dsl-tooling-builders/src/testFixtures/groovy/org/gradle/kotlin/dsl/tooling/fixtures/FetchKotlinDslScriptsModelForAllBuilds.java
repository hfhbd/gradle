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
package org.gradle.kotlin.dsl.tooling.fixtures;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.kotlin.dsl.KotlinDslScriptModel;
import org.gradle.tooling.model.kotlin.dsl.KotlinDslScriptsModel;
import org.gradle.tooling.model.gradle.GradleBuild;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fetches {@link KotlinDslScriptsModel} for the root build and all editable builds,
 * mirroring what the IDE does during sync.
 */
public class FetchKotlinDslScriptsModelForAllBuilds implements BuildAction<Map<File, KotlinDslScriptModel>>, Serializable {

    @Override
    public Map<File, KotlinDslScriptModel> execute(BuildController controller) {
        GradleBuild rootBuild = controller.getModel(GradleBuild.class);
        Map<File, KotlinDslScriptModel> allModels = new LinkedHashMap<>();

        collectScriptModels(controller, rootBuild, allModels);
        for (GradleBuild build : rootBuild.getEditableBuilds()) {
            collectScriptModels(controller, build, allModels);
        }

        return allModels;
    }

    private static void collectScriptModels(BuildController controller, GradleBuild build, Map<File, KotlinDslScriptModel> target) {
        KotlinDslScriptsModel model = controller.getModel(build.getRootProject(), KotlinDslScriptsModel.class);
        target.putAll(model.getScriptModels());
    }
}
