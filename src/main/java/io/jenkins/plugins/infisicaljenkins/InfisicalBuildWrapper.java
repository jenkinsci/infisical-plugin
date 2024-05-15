package io.jenkins.plugins.infisicaljenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfigResolver;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import io.jenkins.plugins.infisicaljenkins.log.MaskingConsoleLogFilter;
import io.jenkins.plugins.infisicaljenkins.model.InfisicalSecret;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import jenkins.tasks.SimpleBuildWrapper;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class InfisicalBuildWrapper extends SimpleBuildWrapper {

  private InfisicalConfiguration configuration;
  private List<String> secretValuesToMask = new ArrayList<>();
  protected transient PrintStream logger;
  // private transient InfisicalAccessor infisicalAccessor = new
  // InfisicalAccessor();

  private List<InfisicalSecret> infisicalSecrets;

  @DataBoundConstructor
  public InfisicalBuildWrapper(@CheckForNull List<InfisicalSecret> infisicalSecrets) {
    this.infisicalSecrets = infisicalSecrets;
  }

  @DataBoundSetter
  public void setConfiguration(InfisicalConfiguration configuration) {
    this.configuration = configuration;
  }

  public InfisicalConfiguration getConfiguration() {
    return this.configuration;
  }

  public List<InfisicalSecret> getInfisicalSecrets() {
    return this.infisicalSecrets;
  }

  @Override
  public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
      EnvVars initialEnvironment) {
    logger = listener.getLogger();
    pullAndMergeConfiguration(build);

    // Check for null, since we shouldn't run the plugin if there are no secrets
    // to fetch.
    if (infisicalSecrets != null) {
      provideEnvironmentVariablesFromInfisical(context, build, initialEnvironment);
    } else {
      logger.println("No secrets to fetch - skipping Infisical Plugin.");
    }
  }

  private void pullAndMergeConfiguration(Run<?, ?> build) {
    for (InfisicalConfigResolver resolver : ExtensionList.lookup(InfisicalConfigResolver.class)) {
      if (configuration != null) {
        configuration = configuration.mergeWithParent(resolver.forJob(build.getParent()));
      } else {
        configuration = resolver.forJob(build.getParent());
      }
    }
    if (configuration == null) {
      throw new InfisicalPluginException("No configuration found - please configure the Infisical Plugin.");
    }
    configuration.fixDefaults();
  }

  protected void provideEnvironmentVariablesFromInfisical(Context context, @SuppressWarnings({ "rawtypes" }) Run build,
      EnvVars envVars) {

    if (configuration == null) {
      throw new InfisicalPluginException("No configuration found - please configure the Infisical Plugin.");
    }

    if (infisicalSecrets == null) {
      throw new InfisicalPluginException("No secrets found - please configure the Infisical Plugin.");
    }

    Map<String, String> variables = new InfisicalAccessor().fetchInfisicalSecrets(build, logger, envVars, configuration,
        infisicalSecrets);

    for (Map.Entry<String, String> entry : variables.entrySet()) {
      secretValuesToMask.add(entry.getValue());
    }

    for (Map.Entry<String, String> entry : variables.entrySet()) {
      // logger.printf("Setting environment variable: %s with value: %s\n",
      // entry.getKey(), entry.getValue());
      context.env(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public ConsoleLogFilter createLoggerDecorator(@NonNull final Run<?, ?> build) {
    return new MaskingConsoleLogFilter(build.getCharset().name(),
        secretValuesToMask);
  }

  /**
   * Descriptor for {@link InfisicalBuildWrapper}. Used as a singleton. The class
   * is
   * marked as public
   * so that it can be accessed from views.
   */
  @Symbol("infisical")
  @Extension
  public static final class DescriptorImpl extends BuildWrapperDescriptor {

    public DescriptorImpl() {
      super(InfisicalBuildWrapper.class);
      load();
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> item) {
      return true;
    }

    /**
     * This human readable name is used in the configuration screen.
     */
    @Override
    public String getDisplayName() {
      return "Infisical Plugin";
    }
  }
}
