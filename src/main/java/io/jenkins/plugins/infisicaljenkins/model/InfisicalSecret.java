package io.jenkins.plugins.infisicaljenkins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

import static hudson.Util.fixEmptyAndTrim;

public class InfisicalSecret extends AbstractDescribableImpl<InfisicalSecret> {

  private String path;
  private boolean includeImports;

  private List<InfisicalSecretValue> secretValues;

  @DataBoundConstructor
  public InfisicalSecret(String path, boolean includeImports, List<InfisicalSecretValue> secretValues) {

    if (path == null || path.isEmpty()) {
      path = DescriptorImpl.DEFAULT_PATH;
    }

    this.includeImports = includeImports;
    this.path = fixEmptyAndTrim(path);

    if (this.path == null) {
      this.path = DescriptorImpl.DEFAULT_PATH;
    }

    // if path doesn't start with / prepend it with /
    if (!this.path.startsWith("/")) {
      this.path = "/" + this.path;
    }

    this.secretValues = secretValues;
  }

  public String getPath() {
    return this.path;
  }

  public boolean getIncludeImports() {
    return this.includeImports;
  }

  public List<InfisicalSecretValue> getSecretValues() {
    return this.secretValues;
  }

  @Extension
  public static final class DescriptorImpl extends Descriptor<InfisicalSecret> {

    public static final String DEFAULT_PATH = "/";
    public static final boolean DEFAULT_INCLUDE_IMPORTS = true;

    @Override
    public String getDisplayName() {
      return "Infisical Secret";
    }
  }

}
