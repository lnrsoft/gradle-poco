/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.integtests.fixtures.publish

import org.gradle.test.fixtures.server.http.MavenHttpRepository

class ModuleSpec {
    private final String groupId
    private final String artifactId
    private final Map<String, ModuleVersionSpec> versions = [:].withDefault { new ModuleVersionSpec(groupId, artifactId, it) }

    private boolean expectGetMetatada

    ModuleSpec(String group, String name) {
        groupId = group
        artifactId = name
    }

    void version(String version, @DelegatesTo(value=ModuleVersionSpec, strategy = Closure.DELEGATE_ONLY) Closure<Void> versionSpec = {}) {
        versionSpec.delegate = versions[version]
        versionSpec.resolveStrategy = Closure.DELEGATE_ONLY
        versionSpec()
    }

    void build(MavenHttpRepository repository) {
        versions.values()*.build(repository)
        if (expectGetMetatada) {
            repository.module(groupId, artifactId).rootMetaData.expectGet()
        }
    }

    void expectVersionListing() {
        expectGetMetadata()
    }

    void expectGetMetadata() {
        expectGetMetatada = true
    }

    void methodMissing(String name, args) {
        Closure spec = {}
        if (args && args.length == 1 && args[0] instanceof Closure) {
            spec = args[0]
        }
        version(name, spec)
    }

}
