package edu.wpi.first.deployutils.files;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileTree;

import java.util.function.Supplier;
import java.util.Set;
import java.util.function.Function;

public class FileTreeSupplier implements Supplier<FileTree> {
  private Set<ResolvedArtifact> resolvedArtifacts;
  private Function<Set<ResolvedArtifact>, FileTree> resolveFunc;
  private Configuration cfg;

  public FileTreeSupplier(Configuration cfg, Function<Set<ResolvedArtifact>, FileTree> resolveFunc) {
    this.cfg = cfg;
    this.resolveFunc = resolveFunc;
  }

  @Override
  public FileTree get() {
    if (resolvedArtifacts == null) {
      resolvedArtifacts = cfg.getResolvedConfiguration().getResolvedArtifacts();
    }
    return resolveFunc.apply(resolvedArtifacts);
  }
}
