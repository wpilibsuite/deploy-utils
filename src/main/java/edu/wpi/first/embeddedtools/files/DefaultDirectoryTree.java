package edu.wpi.first.embeddedtools.files;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree;
import org.gradle.api.internal.file.collections.FileTreeAdapter;
import org.gradle.api.internal.file.collections.MinimalFileTree;

public class DefaultDirectoryTree extends AbstractDirectoryTree {

    private Supplier<FileTree> treeSupplier;
    private List<String> subdirs;

    public DefaultDirectoryTree(Supplier<FileTree> rootTree, List<String> subdirs) {
        this.treeSupplier = rootTree;
        this.subdirs = subdirs;
    }

    public DefaultDirectoryTree(FileTree tree, List<String> subdirs) {
        this(() -> tree, subdirs);
    }

    @Override
    public Set<File> getDirectories() {
        return subdirs.stream().map(subdir -> {
            File rootDir = null;
            FileTree tree = treeSupplier.get();
            if (tree instanceof DirectoryTree) { // project.fileTree
                rootDir = ((DirectoryTree)tree).getDir();
            } else if (tree instanceof FileTreeAdapter) { // project.zipTree
                FileTreeAdapter fTree = (FileTreeAdapter)tree;
                MinimalFileTree lTree = fTree.getTree();
                fTree.visit(details -> details.getFile());
                DirectoryTree dirTree = null;
                if (lTree instanceof FileSystemMirroringFileTree) {
                    dirTree = ((FileSystemMirroringFileTree)lTree).getMirror();
                } else {
                    throw new RuntimeException("Unknown method");
                }
                rootDir = dirTree.getDir();
            } else if (tree instanceof FileCollection) {
                tree = ((FileCollection)tree).getAsFileTree();
                ((FileCollection)tree).getFiles();
            }

            return new File(rootDir, subdir);
        }).collect(Collectors.toSet());
    }
}
