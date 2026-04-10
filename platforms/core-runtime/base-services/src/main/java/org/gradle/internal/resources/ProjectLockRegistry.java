/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.internal.resources;

import org.gradle.util.Path;

public class ProjectLockRegistry extends AbstractResourceLockRegistry<Path, ProjectLock> {
    private final boolean parallelEnabled;
    private final LockCache<Path, AllProjectsLock> allProjectsLocks;

    public ProjectLockRegistry(ResourceLockCoordinationService coordinationService, boolean parallelEnabled) {
        super(coordinationService);
        this.parallelEnabled = parallelEnabled;
        allProjectsLocks = new LockCache<>(coordinationService, this);
    }

    /**
     * Checks if any project lock is currently held that is associated with the given "all projects" lock, and is held by another thread.
     * This is effectively a proxy for checking if any of the project locks have the same {@code buildIdentityPath} as the given "all projects" lock.
     *
     * @param relevantAllProjectsLock the "all projects" lock for which to check if any project locks are currently held by another thread
     * @return {@code true} if any project lock is currently held by another thread that is associated with the given "all projects" lock, {@code false} otherwise
     */
    private boolean isAnyProjectLockHeldByAnotherThread(AllProjectsLock relevantAllProjectsLock) {
        for (ProjectLock projectLock : getAllResourceLocks()) {
            if (projectLock.getAllProjectsLock() != relevantAllProjectsLock) {
                continue;
            }
            if (projectLock.isLocked() && !projectLock.isLockedByCurrentThread()) {
                return true;
            }
        }
        return false;
    }

    public boolean getAllowsParallelExecution() {
        return parallelEnabled;
    }

    public ResourceLock getAllProjectsLock(final Path buildIdentityPath) {
        return allProjectsLocks.getOrRegisterResourceLock(buildIdentityPath, (key, coordinationService, owner) -> {
            String displayName = "All projects of " + buildIdentityPath;
            return new AllProjectsLock(displayName, coordinationService, owner, this::isAnyProjectLockHeldByAnotherThread);
        });
    }

    public ProjectLock getProjectLock(Path buildIdentityPath, Path projectIdentityPath) {
        return doGetResourceLock(buildIdentityPath, parallelEnabled ? projectIdentityPath : buildIdentityPath);
    }

    private ProjectLock doGetResourceLock(final Path buildIdentityPath, final Path lockPath) {
        return getOrRegisterResourceLock(lockPath, (projectPath, coordinationService, owner) -> {
            String displayName = parallelEnabled ? "state of project " + lockPath : "state of build " + lockPath;
            return new ProjectLock(displayName, coordinationService, owner, getAllProjectsLock(buildIdentityPath));
        });
    }
}
