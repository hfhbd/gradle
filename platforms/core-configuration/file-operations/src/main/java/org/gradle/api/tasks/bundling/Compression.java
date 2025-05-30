/*
 * Copyright 2007 the original author or authors.
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
package org.gradle.api.tasks.bundling;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

/**
 * Specifies the compression which should be applied to a TAR archive.
 */
public enum Compression {
    NONE("tar"),
    GZIP("tgz", "gz"),
    BZIP2("tbz2", "bz2");

    private final String defaultExtension;
    private final ImmutableList<String> supportedExtensions;

    private Compression(String defaultExtension, String... additionalSupportedExtensions) {
        this.defaultExtension = defaultExtension;

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.addAll(Arrays.asList(additionalSupportedExtensions));
        builder.add(defaultExtension);

        this.supportedExtensions = builder.build();
    }

    public String getDefaultExtension(){
        return defaultExtension;
    }

    public List<String> getSupportedExtensions(){
        return supportedExtensions;
    }
}
