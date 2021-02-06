package edu.wpi.first.embeddedtools.nativedeps

import groovy.transform.CompileStatic

@CompileStatic
interface SystemLibsDependencySet {
    List<String> getSystemLibs()
}
