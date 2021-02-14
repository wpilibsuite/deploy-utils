package edu.wpi.first.embeddedtools.nativedeps;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.ArtifactView.ViewConfiguration;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.model.Defaults;
import org.gradle.model.Each;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.embeddedtools.SortUtils;
import edu.wpi.first.embeddedtools.files.DefaultDirectoryTree;
import edu.wpi.first.embeddedtools.files.IDirectoryTree;

public class NativeDepsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("ETDependencySpecs", DependencySpecExtension.class, project);
    }

    public static class NativeDepsRules extends RuleSource {
        @Model("libraries")
        public void createLibrariesModel(NativeDepsSpec spec) { }

        @Defaults
        public void setDefaultCombined(@Each CombinedNativeLib combined) {
            combined.setLibs(new ArrayList<>());
        }

        @Mutate
        public void addNativeLibs(ModelMap<Task> tasks,
                            final NativeDepsSpec spec, final ExtensionContainer extensions,
                            final FlavorContainer flavors, final BuildTypeContainer buildTypes, final PlatformContainer platforms,
                            final ProjectLayout projectLayout) {
            Project project = (Project) projectLayout.getProjectIdentifier();
            DependencySpecExtension dse = extensions.getByType(DependencySpecExtension.class);

            for (NativeLib lib : spec.withType(NativeLib.class)) {
                String uniqName = lib.getName();
                String libName = lib.getLibraryName() == null ? uniqName : lib.getLibraryName();

                Supplier<FileTree> rootTree = addDependency(project, lib);

                Flavor flavor = lib.getFlavor() == null ? null : flavors.findByName(lib.getFlavor());
                BuildType buildType = lib.getBuildType() == null ? null : buildTypes.findByName(lib.getBuildType());
                Iterable<NativePlatform> targetPlatforms = getPlatforms(lib, platforms);

                FileCollection sharedFiles = matcher(project, rootTree, lib.getSharedMatchers(), lib.getSharedExcludes());
                FileCollection staticFiles = matcher(project, rootTree, lib.getStaticMatchers(), lib.getStaticExcludes());
                FileCollection debugFiles = matcher(project, rootTree, lib.getDebugMatchers(), lib.getDebugExcludes());
                FileCollection dynamicFiles = matcher(project, rootTree, lib.getDynamicMatchers(), lib.getDynamicExcludes());

                IDirectoryTree headerFiles = new DefaultDirectoryTree(rootTree, lib.getHeaderDirs() == null ? new ArrayList<>() : lib.getHeaderDirs());
                IDirectoryTree sourceFiles = new DefaultDirectoryTree(rootTree, lib.getSourceDirs() == null ? new ArrayList<>() : lib.getSourceDirs());

                for (NativePlatform platform : targetPlatforms) {
                    ETNativeDepSet depSet = new ETNativeDepSet(
                        project, libName,
                        headerFiles, sourceFiles, staticFiles.plus(sharedFiles),
                        dynamicFiles, debugFiles, lib.getSystemLibs() == null ? new ArrayList<>() : lib.getSystemLibs(),
                        platform, flavor, buildType
                    );
                    dse.getSets().add(depSet);
                }
            }

            for (CombinedNativeLib lib : sortCombinedLibs(spec.withType(CombinedNativeLib.class).values())) {
                String uniqName = lib.getName();
                String libName = lib.getLibraryName() == null ? uniqName : lib.getLibraryName();

                Iterable<Flavor> targetFlavors = getFlavors(lib, flavors);
                Iterable<BuildType> targetBuildTypes = getBuildTypes(lib, buildTypes);
                Iterable<NativePlatform> targetPlatforms = getPlatforms(lib, platforms);

                for (NativePlatform platform : targetPlatforms) {
                    for (Flavor flavor : targetFlavors) {
                        for (BuildType buildType : targetBuildTypes) {
                            ETNativeDepSet dep = mergedDepSet(project, dse, libName, lib.getLibs(), flavor, buildType, platform);
                            dse.getSets().add(dep);
                        }
                    }
                }
            }
        }

        @BinaryTasks
        void addLinkerArgs(ModelMap<Task> tasks, final NativeBinarySpec binary) {
            tasks.withType(AbstractLinkTask.class, task -> {
                task.getLinkerArgs().addAll(
                    task.getProject().getProviders().provider(new Callable<List<String>>() {
                        @Override
                        public List<String> call() throws Exception {
                            List<String> libs = new ArrayList<>();
                            for (NativeDependencySet lib : binary.getLibs()) {
                                if (lib instanceof DelegatedDependencySet) {
                                    DelegatedDependencySet set = (DelegatedDependencySet)lib;
                                    libs.addAll(set.getSystemLibs());
                                }
                            }
                            return libs;
                        }
                    })
                );
            });
        }

        private static Supplier<FileTree> addDependency(Project proj, NativeLib lib) {
            String config = lib.getConfiguration() == null ? "native_" + lib.getName() : lib.getConfiguration();
            Configuration cfg = proj.getConfigurations().maybeCreate(config);
            proj.getDependencies().registerTransform(UnzipTransform.class, new Action<TransformSpec<TransformParameters.None>>() {
				@Override
				public void execute(TransformSpec<TransformParameters.None> variantTransform) {
                    variantTransform.getFrom().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ZIP_TYPE);
                    variantTransform.getTo().attribute(ArtifactAttributes.ARTIFACT_FORMAT, DIRECTORY_TYPE);
				}
            });
            if (lib.getMaven() != null) {
                proj.getDependencies().add(config, lib.getMaven());
                ArtifactView includeDirs = cfg.getIncoming().artifactView(new Action<ViewConfiguration>() {
					@Override
					public void execute(ViewConfiguration viewConfiguration) {
                        viewConfiguration.attributes(new Action<AttributeContainer>() {

							@Override
                            public void execute(AttributeContainer attributeContainer) {
                                attributeContainer.attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.DIRECTORY_TYPE);
							}

                        });
					}
                });
                return new Supplier<FileTree>() {
					@Override
					public FileTree get() {
						return proj.fileTree(includeDirs.getFiles().getSingleFile());
					}
                };
            } else if (lib.getFile() != null && lib.getFile().isDirectory()) {
                // File is a directory
                return new Supplier<FileTree>() {
					@Override
					public FileTree get() {
						return proj.fileTree(lib.getFile());
					}
                };
            } else if (lib.getFile() != null && lib.getFile().isFile()) {
                return new Supplier<FileTree>() {
					@Override
					public FileTree get() {
						return proj.getRootProject().zipTree(lib.getFile());
					}
                };
            } else {
                throw new GradleException("No target defined for dependency " + lib.getName() + " (maven=" + lib.getMaven() + " file=" + lib.getFile() + ")");
            }
        }

        private static FileCollection matcher(Project proj, Supplier<FileTree> tree, List<String> matchers, List<String> excludes) {
            return proj.files(new Callable<FileCollection>() {

				@Override
				public FileCollection call() throws Exception {
                    return tree.get().matching(new Action<PatternFilterable>() {

						@Override
						public void execute(PatternFilterable filter) {
                            // <<!!ET_NOMATCH!!> is a magic string in the case the matchers are null.
                            // This is because, without include, the filter will include all files
                            // by default. We don't want this behavior.
                            filter.include(matchers == null || matchers.isEmpty() ? List.of("<<!!ET_NOMATCH!!>") : matchers);
                            filter.exclude(excludes == null || excludes.isEmpty() ? List.of() : excludes);
						}

                    });
				}

            });
        }

        private static List<CombinedNativeLib> sortCombinedLibs(Collection<CombinedNativeLib> libs) {
            List<SortUtils.TopoMember<CombinedNativeLib>> unsorted =
                libs.stream()
                    .map(lib -> new SortUtils.TopoMember<CombinedNativeLib>(lib.getName(), lib.getLibs(), lib))
                    .collect(Collectors.toList());
            return SortUtils.topoSort(unsorted);
        }

        private static ETNativeDepSet mergedDepSet(Project proj, DependencySpecExtension dse, String name, List<String> libNames,
                                                   Flavor flavor, BuildType buildType, NativePlatform platform) {

            List<ETNativeDepSet> libs = libNames.stream().map(it -> dse.find(it, flavor, buildType, platform)).collect(Collectors.toList());
            IDirectoryTree headers = libs.stream().map(x -> x.getHeaders()).reduce((a, b) -> a.plus(b)).get();
            IDirectoryTree sources = libs.stream().map(x -> x.getSources()).reduce((a, b) -> a.plus(b)).get();
            FileCollection linkFiles =  libs.stream().map(x -> x.getLinkLibs()).reduce((a, b) -> a.plus(b)).get();
            FileCollection debugFiles = libs.stream().map(x -> x.getDebugLibs()).reduce((a, b) -> a.plus(b)).get();
            FileCollection dynamicFiles = libs.stream().map(x -> x.getDynamicLibs()).reduce((a, b) -> a.plus(b)).get();
            List<String> systemLibs = libs.stream().map(x -> x.getSystemLibs()).flatMap(List<String>::stream).collect(Collectors.toList());

            return new ETNativeDepSet(
                    proj, name,
                    headers, sources, linkFiles,
                    dynamicFiles, debugFiles, systemLibs,
                    platform, flavor, buildType
            );
        }

        private static <T> List<T> nullArrayList(Class<T> cls) {
            List<T> list = new ArrayList<>();
            list.add(null);
            return list;
        }

        private static Iterable<Flavor> getFlavors(BaseLibSpec lib, final FlavorContainer flavors) {
            if (lib.getFlavor() == null && (lib.getFlavors() == null || lib.getFlavors().isEmpty()))
                return () -> nullArrayList(Flavor.class).iterator();
            List<String> fl = lib.getFlavors() == null ? List.of(lib.getFlavor()) : lib.getFlavors();
            return () -> fl.stream().map(x -> flavors.findByName(x)).filter(x -> x != null).iterator();
        }

        private static Iterable<BuildType> getBuildTypes(BaseLibSpec lib, final BuildTypeContainer buildTypes) {
            if (lib.getBuildType() == null && (lib.getBuildTypes() == null || lib.getBuildTypes().isEmpty()))
                return () -> nullArrayList(BuildType.class).iterator();
            List<String> fl = lib.getBuildTypes() == null ? List.of(lib.getBuildType()) : lib.getBuildTypes();
            return () -> fl.stream().map(x -> buildTypes.findByName(x)).filter(x -> x != null).iterator();
        }

        private static Iterable<NativePlatform> getPlatforms(BaseLibSpec lib, final PlatformContainer platforms) {
            if (lib.getTargetPlatform() == null && (lib.getTargetPlatforms() == null || lib.getTargetPlatforms().isEmpty()))
                return () -> nullArrayList(NativePlatform.class).iterator();
            List<String> fl = lib.getTargetPlatforms() == null ? List.of(lib.getTargetPlatform()) : lib.getTargetPlatforms();
            return () -> fl.stream().map(x -> (NativePlatform)platforms.findByName(x)).filter(x -> x != null).iterator();
        }
    }
}
