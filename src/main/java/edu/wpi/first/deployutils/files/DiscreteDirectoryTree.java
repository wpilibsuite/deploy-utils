package edu.wpi.first.deployutils.files;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DiscreteDirectoryTree extends AbstractDirectoryTree {

    private Set<File> set;

    public Set<File> getSet() {
        return set;
    }

    public void setSet(Set<File> set) {
        this.set = set;
    }

    public DiscreteDirectoryTree() {
        set = new HashSet<>();
    }

    public DiscreteDirectoryTree(Set<File> set) {
        this.set = set;
    }

    public void add(File f) {
        set.add(f);
    }

    @Override
    public Set<File> getDirectories() {
        return set;
    }

}
