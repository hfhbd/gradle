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

package org.gradle.internal.logging.console

import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class ConsoleNoColorIntegrationTest extends AbstractIntegrationSpec {

    def "NO_COLOR strips foreground colors with --console=#consoleOutput"() {
        given:
        executer.withEnvironmentVars(NO_COLOR: "1")
        executer.withConsole(consoleOutput)

        when:
        succeeds('help')

        then:
        !(output =~ /\u001B\[3[0-7]m/)
        !(output =~ /\u001B\[9[0-7]m/)

        where:
        consoleOutput << ConsoleOutput.values()
    }

    def "NO_COLOR preserves bold styling with --console=#consoleOutput"() {
        given:
        executer.withEnvironmentVars(NO_COLOR: "1")
        executer.withConsole(consoleOutput)

        when:
        succeeds('help')

        then:
        output.contains('\u001B[1m')

        where:
        consoleOutput << [ConsoleOutput.Rich, ConsoleOutput.Verbose, ConsoleOutput.Colored]
    }
}
