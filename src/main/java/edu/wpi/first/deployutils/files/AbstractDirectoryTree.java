package edu.wpi.first.deployutils.files;

public abstract class AbstractDirectoryTree implements IDirectoryTree {

    public IDirectoryTree plus(IDirectoryTree other) {
        return new CombinedDirectoryTree(this, other);
    }

}
