package edu.wpi.first.embeddedtools.toolchains;

import edu.wpi.first.embeddedtools.log.ETLogger;
import edu.wpi.first.embeddedtools.log.ETLoggerFactory;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.TreeFormatter;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.registry.LanguageTransform;
import org.gradle.language.base.internal.registry.LanguageTransformContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.internal.BinarySpecInternal;

public class ToolchainsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin.class);
        project.getExtensions().create("toolchainUtil", ToolchainUtilExtension.class);

    }

    public static class ToolchainUtilExtension {
        private boolean skipBinaryToolchainMissingWarning = false;

        public boolean getSkipBinaryToolchainMissingWarning() {
            return skipBinaryToolchainMissingWarning;
        }

        public void setSkipBinaryToolchainMissingWarning(boolean skipBinaryToolchainMissingWarning) {
            this.skipBinaryToolchainMissingWarning = skipBinaryToolchainMissingWarning;
        }

        public void defineGccTools(GccPlatformToolChain platformToolchain, String prefix, String suffix) {
            platformToolchain.getCppCompiler().setExecutable(prefix + platformToolchain.getCppCompiler().getExecutable() + suffix);
            platformToolchain.getcCompiler().setExecutable(prefix + platformToolchain.getcCompiler().getExecutable() + suffix);
            platformToolchain.getLinker().setExecutable(prefix + platformToolchain.getLinker().getExecutable() + suffix);
            platformToolchain.getAssembler().setExecutable(prefix + platformToolchain.getAssembler().getExecutable() + suffix);
            platformToolchain.getStaticLibArchiver().setExecutable(prefix + platformToolchain.getStaticLibArchiver().getExecutable() + suffix);
        }
    }

    public static class ToolchainRules extends RuleSource {
        static final Map<String, ToolType> LANG_TOOLS_MAP = Map.of(
            "cpp", ToolType.CPP_COMPILER,
            "c", ToolType.C_COMPILER,
            "objcpp",ToolType.OBJECTIVECPP_COMPILER,
            "objc",ToolType.OBJECTIVEC_COMPILER,
            "rc",ToolType.WINDOW_RESOURCES_COMPILER,
            "asm",ToolType.ASSEMBLER
            );

        public static void markUnavailable(ETLogger log, NativeBinarySpec bin, String reason, boolean disable, boolean error, boolean doPrint) {
            String msg = "Skipping build: " + bin + ": " + reason;
            if (doPrint) {
                if (error)
                    log.logErrorHead(msg);
                else
                    log.logStyle(msg, StyledTextOutput.Style.Info);
            }

            if (disable) {
                ((BinarySpecInternal)bin).setBuildable(false);
            }
        }

        // TODO: drive this logic based on the platform (i.e. OptionalNativePlatform)
        public static void configureOptional(NativeBinarySpec bin, LanguageTransformContainer langTransforms, ToolchainUtilExtension tcExt) {
            ETLogger log = ETLoggerFactory.INSTANCE.create("ToolchainRules");

            log.debug("Configuring optionals for binary: " + bin);
            NativeToolChainInternal tc = (NativeToolChainInternal)bin.getToolChain();
            PlatformToolProvider toolProvider = tc.select((NativePlatformInternal)bin.getTargetPlatform());
            log.debug("Tool Provider: " + toolProvider);

            if (toolProvider.isAvailable()) {
                for (LanguageSourceSet ss : bin.getInputs()) {
                    log.debug("Querying transforms for input: " + ss);

                    boolean hasTransform = false;
                    for (LanguageTransform<?, ?> transform : langTransforms) {
                        log.debug("Querying transform " + transform);
                        if (transform.getSourceSetType().isInstance(ss)) {
                            hasTransform = true;
                            ToolType requiresTool = LANG_TOOLS_MAP.get(transform.getLanguageName());
                            log.debug("Found transform: " + transform.getLanguageName());
                            log.debug("Requires tool: " + requiresTool);

                            if (requiresTool.equals(ToolType.WINDOW_RESOURCES_COMPILER)) {
                                continue;
                            }

                            CommandLineToolSearchResult searchResult = toolProvider.locateTool(requiresTool);
                            if (!searchResult.isAvailable()) {
                                markUnavailable(log, bin, "Toolchain " + tc.getName() + " cannot build " + bin.getTargetPlatform().getName() + " (tool " + requiresTool + " not found)", true, true, true);
                                log.info("Could not find tool: " + requiresTool);
                                TreeFormatter fmt = new TreeFormatter();
                                searchResult.explain(fmt);
                                log.info(fmt.toString());
                            }
                        }
                    }
                    if (!hasTransform)
                        markUnavailable(log, bin, "Binary does not have a language transform for input " + ss, true, true, true);
                }
                if (bin.getInputs().isEmpty()) {
                    markUnavailable(log, bin, "Binary has no inputs", true, true, true);
                }
            } else {
                // Gradle automatically disables cases where a toolchain can't be found for this platform.
                markUnavailable(log, bin, "Could not find valid toolchain for platform " + bin.getTargetPlatform().getName(), false, false, !tcExt.skipBinaryToolchainMissingWarning);
            }
        }

        @Mutate
        public void configureOptionalBuildables(BinaryContainer binaries, LanguageTransformContainer languageTransforms, final ExtensionContainer ext) {
            ToolchainUtilExtension tcExt = ext.getByType(ToolchainUtilExtension.class);
            binaries.withType(NativeBinarySpec.class, bin -> {
                configureOptional(bin, languageTransforms, tcExt);;
            });
        }
    }
}
