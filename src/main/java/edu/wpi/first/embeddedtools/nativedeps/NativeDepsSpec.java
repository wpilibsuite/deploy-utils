package edu.wpi.first.embeddedtools.nativedeps;

import org.gradle.model.Managed;
import org.gradle.model.ModelMap;

@Managed
public interface NativeDepsSpec extends ModelMap<BaseLibSpec> { }
