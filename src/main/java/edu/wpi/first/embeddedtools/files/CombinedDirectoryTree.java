package edu.wpi.first.embeddedtools.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CombinedDirectoryTree extends AbstractDirectoryTree {

    private List<IDirectoryTree> subtrees;

    public List<IDirectoryTree> getSubtrees() {
        return subtrees;
    }

    public CombinedDirectoryTree() {
        subtrees = new ArrayList<>();
    }

    public CombinedDirectoryTree(IDirectoryTree... trees) {
        subtrees = new ArrayList<>(Arrays.asList(trees));
    }

    public void add(IDirectoryTree tree) {
        subtrees.add(tree);
    }

    @Override
    public Set<File> getDirectories() {
        return subtrees.stream().map(t -> t.getDirectories()).flatMap(x -> x.stream()).collect(Collectors.toSet());
    }
}
