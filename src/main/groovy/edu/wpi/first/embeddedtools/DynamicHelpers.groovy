package edu.wpi.first.embeddedtools;

import org.gradle.nativeplatform.NativeBinarySpec;

public class DynamicHelpers {
    public static void setBuildable(NativeBinarySpec spec, boolean value) {
        spec.buildable = value
    }
}
