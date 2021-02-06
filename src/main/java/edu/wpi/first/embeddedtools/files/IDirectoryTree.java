package edu.wpi.first.embeddedtools.files;

import java.io.File;
import java.util.Set;

public interface IDirectoryTree {
    Set<File> getDirectories();
    IDirectoryTree plus(IDirectoryTree other);
}
