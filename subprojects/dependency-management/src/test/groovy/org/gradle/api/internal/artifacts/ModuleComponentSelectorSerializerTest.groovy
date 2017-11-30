/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.artifacts

import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.internal.serialize.SerializerSpec
import spock.lang.Unroll

import static org.gradle.internal.component.external.model.DefaultModuleComponentSelector.newSelector

class ModuleComponentSelectorSerializerTest extends SerializerSpec {
    private serializer = new ModuleComponentSelectorSerializer()

    @Unroll
    def "serializes"() {
        when:
        def result = serialize(newSelector("org", "foo", new DefaultMutableVersionConstraint(version, rejects)), serializer)

        then:
        result == newSelector("org", "foo", new DefaultMutableVersionConstraint(version, rejects))

        where:
        version | rejects
        '5.0'   | []
        '5.0'   | ['1.0']
        '5.0'   | ['1.0', '2.0']

    }
}
