package io.jenkins.plugins.infisicaljenkins.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;

public abstract class InfisicalConfigResolver implements ExtensionPoint {

  @NonNull
  public abstract InfisicalConfiguration forJob(@NonNull Item job);

  public abstract InfisicalConfiguration getInfisicalConfig(@NonNull ItemGroup<Item> itemGroup);
}
