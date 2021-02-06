package edu.wpi.first.embeddedtools.nativedeps;

import java.util.List;

import org.gradle.model.Managed;

@Managed
public interface CombinedNativeLib extends BaseLibSpec {
    void setLibs(List<String> libs);
    List<String> getLibs();
}
