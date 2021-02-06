package edu.wpi.first.embeddedtools.deploy.cache

import groovy.transform.CompileStatic
import edu.wpi.first.embeddedtools.deploy.context.DeployContext

@CompileStatic
@FunctionalInterface
interface CacheCheckerFunction {
  boolean check(DeployContext ctx, String filename, File localFile)
}
