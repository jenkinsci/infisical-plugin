package io.jenkins.plugins.infisicaljenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static hudson.Util.fixEmptyAndTrim;

public class InfisicalSecretValue
    extends AbstractDescribableImpl<InfisicalSecretValue> {

  private boolean isRequired = DescriptorImpl.DEFAULT_IS_REQUIRED;
  private String infisicalKey;

  @Deprecated
  public InfisicalSecretValue(String envVar, @NonNull String infisicalKey) {
    this.infisicalKey = fixEmptyAndTrim(infisicalKey);
  }

  @DataBoundConstructor
  public InfisicalSecretValue(@NonNull String infisicalKey) {
    this.infisicalKey = fixEmptyAndTrim(infisicalKey);
  }

  @DataBoundSetter
  public void setIsRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }

  @DataBoundSetter
  public void setInfisicalKey(String infisicalKey) {
    this.infisicalKey = fixEmptyAndTrim(infisicalKey);
  }

  public String getInfisicalKey() {
    return infisicalKey;
  }

  public boolean getIsRequired() {
    return isRequired;
  }

  @Extension
  public static final class DescriptorImpl
      extends Descriptor<InfisicalSecretValue> {

    public final static Boolean DEFAULT_IS_REQUIRED = true;

    @Override
    public String getDisplayName() {
      return "Infisical secret key";
    }
  }

}
