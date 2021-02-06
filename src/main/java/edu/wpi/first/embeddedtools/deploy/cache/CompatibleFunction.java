package edu.wpi.first.embeddedtools.deploy.cache;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;

@FunctionalInterface
public interface CompatibleFunction {
  boolean check(DeployContext ctx);
}
