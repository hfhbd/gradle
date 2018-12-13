/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.changedetection.changes;

import com.google.common.collect.ImmutableSortedMap;
import org.gradle.StartParameter;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.changedetection.TaskArtifactState;
import org.gradle.api.internal.changedetection.TaskArtifactStateRepository;
import org.gradle.api.internal.tasks.execution.TaskProperties;
import org.gradle.api.specs.AndSpec;
import org.gradle.internal.change.Change;
import org.gradle.internal.change.ChangeVisitor;
import org.gradle.internal.execution.history.AfterPreviousExecutionState;
import org.gradle.internal.execution.history.BeforeExecutionState;
import org.gradle.internal.execution.history.changes.ExecutionStateChanges;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.reflect.Instantiator;

import javax.annotation.Nullable;
import java.util.Optional;

public class ShortCircuitTaskArtifactStateRepository implements TaskArtifactStateRepository {

    private final StartParameter startParameter;
    private final TaskArtifactStateRepository repository;
    private final Instantiator instantiator;

    public ShortCircuitTaskArtifactStateRepository(StartParameter startParameter, Instantiator instantiator, TaskArtifactStateRepository repository) {
        this.startParameter = startParameter;
        this.instantiator = instantiator;
        this.repository = repository;
    }

    public TaskArtifactState getStateFor(final TaskInternal task, TaskProperties taskProperties) {

        // Only false if no declared outputs AND no Task.upToDateWhen spec. We force to true for incremental tasks.
        AndSpec<? super TaskInternal> upToDateSpec = task.getOutputs().getUpToDateSpec();
        if (!taskProperties.hasDeclaredOutputs() && upToDateSpec.isEmpty()) {
            if (task.hasTaskActions()) {
                return NoOutputsArtifactState.WITH_ACTIONS;
            } else {
                return NoOutputsArtifactState.WITHOUT_ACTIONS;

            }
        }

        TaskArtifactState state = repository.getStateFor(task, taskProperties);

        if (startParameter.isRerunTasks()) {
            return new RerunTaskArtifactState(state, task, "Executed with '--rerun-tasks'.");
        }

        if (!upToDateSpec.isSatisfiedBy(task)) {
            return new RerunTaskArtifactState(state, task, "Task.upToDateWhen is false.");
        }

        return state;
    }

    private class RerunTaskArtifactState implements TaskArtifactState {
        private final TaskArtifactState delegate;
        private final TaskInternal task;
        private final Change reason;

        private RerunTaskArtifactState(TaskArtifactState delegate, TaskInternal task, final String reason) {
            this.delegate = delegate;
            this.task = task;
            this.reason = new Change() {
                @Override
                public String getMessage() {
                    return reason;
                }
            };
        }

        @Override
        public Optional<ExecutionStateChanges> getExecutionStateChanges(@Nullable AfterPreviousExecutionState afterPreviousExecutionState, boolean outputsRemoved) {
            return Optional.<ExecutionStateChanges>of(new ExecutionStateChanges() {
                @Override
                public void visitAllChanges(ChangeVisitor visitor) {
                    visitor.visitChange(reason);
                }

                @Override
                public boolean isRebuildRequired() {
                    return true;
                }

                @Override
                public Iterable<Change> getInputFilesChanges() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public AfterPreviousExecutionState getPreviousExecution() {
                    throw new UnsupportedOperationException();
                }
            });
        }

        @Override
        public boolean isAllowedToUseCachedResults() {
            return false;
        }

        @Override
        public ImmutableSortedMap<String, CurrentFileCollectionFingerprint> getOutputFilesBeforeExecution() {
            return delegate.getOutputFilesBeforeExecution();
        }

        @Override
        public Optional<BeforeExecutionState> getBeforeExecutionState(@Nullable AfterPreviousExecutionState afterPreviousExecutionState) {
            return delegate.getBeforeExecutionState(afterPreviousExecutionState);
        }
    }
}
