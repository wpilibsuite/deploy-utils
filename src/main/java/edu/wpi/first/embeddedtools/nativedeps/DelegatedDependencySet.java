package edu.wpi.first.embeddedtools.nativedeps;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

public class DelegatedDependencySet implements NativeDependencySet, SystemLibsDependencySet {

    private final String name;
    private final NativeBinarySpec binary;
    private final DependencySpecExtension ext;
    private final boolean skipOnNonFoundDependency;

    public String getName() {
        return name;
    }

    public NativeBinarySpec getBinary() {
        return binary;
    }

    public DependencySpecExtension getExt() {
        return ext;
    }

    public boolean isSkipOnNonFoundDependency() {
        return skipOnNonFoundDependency;
    }

    public DelegatedDependencySet(String name, NativeBinarySpec bin, DependencySpecExtension ext) {
        this.name = name;
        this.binary = bin;
        this.ext = ext;
        this.skipOnNonFoundDependency = false;
    }

    public DelegatedDependencySet(String name, NativeBinarySpec bin, DependencySpecExtension ext, boolean skipUd) {
        this.name = name;
        this.binary = bin;
        this.ext = ext;
        this.skipOnNonFoundDependency = skipUd;
    }

    public ETNativeDepSet get() {
        ETNativeDepSet ds = ext.find(name, binary);
        if (ds == null && !skipOnNonFoundDependency)
            throw new MissingDependencyException(name, binary);
        return ds;
    }

    @Override
    public FileCollection getIncludeRoots() {
        ETNativeDepSet depSet =  get();
        if (depSet == null) {
            return ext.getProject().files();
        }
        return depSet.getIncludeRoots();
    }

    @Override
    public FileCollection getLinkFiles() {
        ETNativeDepSet depSet =  get();
        if (depSet == null) {
            return ext.getProject().files();
        }
        return depSet.getLinkFiles();
    }

    @Override
    public FileCollection getRuntimeFiles() {
        ETNativeDepSet depSet =  get();
        if (depSet == null) {
            return ext.getProject().files();
        }
        return depSet.getRuntimeFiles();
    }

    public FileCollection getSourceFiles() {
        ETNativeDepSet depSet =  get();
        if (depSet == null) {
            return ext.getProject().files();
        }
        return depSet.getSourceRoots();
    }

    public FileCollection getDebugFiles() {
        ETNativeDepSet depSet =  get();
        if (depSet == null) {
            return ext.getProject().files();
        }
        return depSet.getDebugFiles();
    }

    @Override
    public List<String> getSystemLibs() {
        ETNativeDepSet depSet =  get();
        if (depSet == null) {
            return new ArrayList<>();
        }
        return depSet.getSystemLibs();
    }

    public static class MissingDependencyException extends RuntimeException {
        private static final long serialVersionUID = -5353892872101329101L;
        private String dependencyName;
        private NativeBinarySpec binary;

        public String getDependencyName() {
            return dependencyName;
        }

        public NativeBinarySpec getBinary() {
            return binary;
        }

        public MissingDependencyException(String name, NativeBinarySpec binary) {
            super("Cannot find delegated dependency: " + name + " for binary: " + binary);
            this.dependencyName = name;
            this.binary = binary;
        }
    }
}
