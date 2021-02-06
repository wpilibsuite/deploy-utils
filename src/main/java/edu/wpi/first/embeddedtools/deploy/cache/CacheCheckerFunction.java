package edu.wpi.first.embeddedtools.deploy.cache;

import java.io.File;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;

@FunctionalInterface
public interface CacheCheckerFunction {
  boolean check(DeployContext ctx, String filename, File localFile);
}
