package edu.wpi.first.embeddedtools.files;

public abstract class AbstractDirectoryTree implements IDirectoryTree {

    public IDirectoryTree plus(IDirectoryTree other) {
        return new CombinedDirectoryTree(this, other);
    }

}
