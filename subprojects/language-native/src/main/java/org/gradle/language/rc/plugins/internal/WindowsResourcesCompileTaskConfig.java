/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.language.rc.plugins.internal;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.collections.MinimalFileSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.LanguageSourceSetInternal;
import org.gradle.language.base.internal.SourceTransformTaskConfig;
import org.gradle.language.rc.WindowsResourceSet;
import org.gradle.language.rc.tasks.WindowsResourceCompile;
import org.gradle.nativeplatform.PreprocessingTool;
import org.gradle.nativeplatform.internal.NativeBinarySpecInternal;
import org.gradle.nativeplatform.internal.StaticLibraryBinarySpecInternal;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.SystemIncludesAwarePlatformToolProvider;
import org.gradle.platform.base.BinarySpec;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class WindowsResourcesCompileTaskConfig implements SourceTransformTaskConfig {
    @Override
    public String getTaskPrefix() {
        return "compile";
    }

    @Override
    public Class<? extends DefaultTask> getTaskType() {
        return WindowsResourceCompile.class;
    }

    @Override
    public void configureTask(Task task, BinarySpec binary, LanguageSourceSet sourceSet, ServiceRegistry serviceRegistry) {
        configureResourceCompileTask((WindowsResourceCompile) task, (NativeBinarySpecInternal) binary, (WindowsResourceSet) sourceSet);
    }

    private void configureResourceCompileTask(WindowsResourceCompile task, final NativeBinarySpecInternal binary, final WindowsResourceSet sourceSet) {
        task.setDescription("Compiles resources of the " + sourceSet + " of " + binary);

        task.setToolChain(binary.getToolChain());
        task.setTargetPlatform(binary.getTargetPlatform());

        task.includes(sourceSet.getExportedHeaders().getSourceDirectories());

        FileCollectionFactory fileCollectionFactory = ((ProjectInternal) task.getProject()).getServices().get(FileCollectionFactory.class);
        task.includes(fileCollectionFactory.create(new MinimalFileSet() {
            @Override
            public Set<File> getFiles() {
                PlatformToolProvider platformToolProvider = ((NativeToolChainInternal) binary.getToolChain()).select((NativePlatformInternal) binary.getTargetPlatform());
                if (platformToolProvider instanceof SystemIncludesAwarePlatformToolProvider) {
                    return new LinkedHashSet<File>(((SystemIncludesAwarePlatformToolProvider) platformToolProvider).getSystemIncludes());
                }
                return ImmutableSet.of();
            }

            @Override
            public String getDisplayName() {
                return "System includes for " + binary.getToolChain().getDisplayName();
            }
        }));

        task.source(sourceSet.getSource());

        final Project project = task.getProject();
        task.setOutputDir(new File(binary.getNamingScheme().getOutputDirectory(project.getBuildDir(), "objs"), ((LanguageSourceSetInternal) sourceSet).getProjectScopedName()));

        PreprocessingTool rcCompiler = (PreprocessingTool) binary.getToolByName("rcCompiler");
        task.setMacros(rcCompiler.getMacros());
        task.setCompilerArgs(rcCompiler.getArgs());

        FileTree resourceOutputs = task.getOutputs().getFiles().getAsFileTree().matching(new PatternSet().include("**/*.res"));
        binary.binaryInputs(resourceOutputs);
        if (binary instanceof StaticLibraryBinarySpecInternal) {
            ((StaticLibraryBinarySpecInternal) binary).additionalLinkFiles(resourceOutputs);
        }
    }

}
