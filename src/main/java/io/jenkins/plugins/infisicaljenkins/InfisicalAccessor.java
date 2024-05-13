package io.jenkins.plugins.infisicaljenkins;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;

import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.model.Run;
import hudson.security.ACL;

import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfigResolver;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import io.jenkins.plugins.infisicaljenkins.credentials.InfisicalCredential;
import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import io.jenkins.plugins.infisicaljenkins.infisical.InfisicalSecrets;
import io.jenkins.plugins.infisicaljenkins.model.InfisicalSecret;
import io.jenkins.plugins.infisicaljenkins.model.InfisicalSecretValue;
import io.jenkins.plugins.infisicaljenkins.model.SingleSecretResponse;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

public class InfisicalAccessor implements Serializable {

  private static final long serialVersionUID = 1L;

  private InfisicalCredential credential;
  private InfisicalConfiguration configuration;

  public InfisicalAccessor() {
    // empty for now i guess
  }

  public InfisicalAccessor(InfisicalCredential credential) {
    this.credential = credential;
  }

  public InfisicalCredential getCredential() {
    return credential;
  }

  public void setCredential(InfisicalCredential credential) {
    this.credential = credential;
  }

  public void setConfiguration(InfisicalConfiguration configuration) {
    this.configuration = configuration;
  }

  public InfisicalConfiguration getConfiguration() {
    return configuration;
  }

  private List<SingleSecretResponse> readSecretsFromInfisical(InfisicalSecret infisicalSecret, PrintStream logger) {

    // Fetch all secrets in the project, environment, and path
    List<SingleSecretResponse> allSecretsInPath = InfisicalSecrets.getSecrets(configuration, this.credential,
        infisicalSecret.getPath(), infisicalSecret.getIncludeImports(), logger);

    logger.printf("Found %d secrets in path: %s%n", allSecretsInPath.size(), infisicalSecret.getPath());

    // Create a key-secret map for fast(er) lookup
    Map<String, SingleSecretResponse> map = new HashMap<>();
    for (SingleSecretResponse fetchedSecret : allSecretsInPath) {
      map.put(fetchedSecret.getSecretKey(), fetchedSecret);
    }

    List<InfisicalSecretValue> infisicalSecretValues = infisicalSecret.getSecretValues();
    List<SingleSecretResponse> secretsToReturn = new ArrayList<>();

    infisicalSecretValues.forEach(secretValue -> {
      SingleSecretResponse matchedSecret = map.get(secretValue.getInfisicalKey());
      if (matchedSecret != null) {
        secretsToReturn.add(matchedSecret);
      }
    });

    return secretsToReturn;

  }

  public Map<String, String> fetchInfisicalSecrets(Run<?, ?> run, PrintStream logger, EnvVars envVars,
      InfisicalConfiguration initialConfiguration,
      List<InfisicalSecret> infisicalSecrets) {
    Map<String, String> environmentVariables = new HashMap<>();

    InfisicalConfiguration config = pullAndMergeConfiguration(run,
        initialConfiguration);
    String url = config.getInfisicalUrl();

    if (StringUtils.isBlank(url)) {
      throw new InfisicalPluginException(
          "The infisical url was not configured - please specify the infisical url to use.");
    }

    this.credential = this.getCredential(run, config);

    this.setCredential(credential);
    this.setConfiguration(initialConfiguration);

    for (InfisicalSecret infisicalSecret : infisicalSecrets) {
      logger.printf("Retrieving secrets from path: %s%n", infisicalSecret.getPath());

      try {
        List<SingleSecretResponse> fetchedSecrets = this.readSecretsFromInfisical(infisicalSecret, logger);

        for (SingleSecretResponse fetchedSecret : fetchedSecrets) {
          String environmentVariableKey = fetchedSecret.getSecretKey();
          String environmentVariableValue = fetchedSecret.getSecretValue();

          if (StringUtils.isBlank(environmentVariableKey)) {
            logger.println("Skipping secret with empty key");
            continue;
          }

          if (StringUtils.isBlank(environmentVariableValue)) {
            logger.println("Skipping secret with empty value");
            continue;
          }
          environmentVariables.put(environmentVariableKey, environmentVariableValue);
        }

      } catch (InfisicalPluginException ex) {
        InfisicalPluginException e = (InfisicalPluginException) ex.getCause();
        if (e != null) {
          throw new InfisicalPluginException(e.getMessage(), e);
        }
        throw ex;
      }

      // here we need to check if the secrets in the infisicalSecret.getSecretValues()
      // are required. For the required secrets, we need to check if they are present
      // in the environmentVariables. If they are not present, we need to throw an
      // exception.

      for (InfisicalSecretValue secretValue : infisicalSecret.getSecretValues()) {
        if (secretValue.getIsRequired()) {
          if (!environmentVariables.containsKey(secretValue.getInfisicalKey())) {
            throw new InfisicalPluginException(
                String.format("Secret %s is required but not found in Infisical in path %s",
                    secretValue.getInfisicalKey(), infisicalSecret.getPath()));
          }
        }
      }
    }

    return environmentVariables;
  }

  private InfisicalCredential getCredential(@SuppressWarnings({ "rawtypes" }) Run build,
      InfisicalConfiguration config) {
    if (Jenkins.getInstanceOrNull() != null) {
      String id = config.getInfisicalCredentialId();

      if (StringUtils.isBlank(id)) {
        throw new InfisicalPluginException(
            "The credential id was not configured - please specify the credentials to use.");
      }

      @SuppressWarnings({ "deprecation" })
      List<InfisicalCredential> credentials = CredentialsProvider.lookupCredentials(InfisicalCredential.class,
          build.getParent(), ACL.SYSTEM,
          Collections.emptyList());
      InfisicalCredential credential = CredentialsMatchers
          .firstOrNull(credentials, new IdMatcher(id));

      if (credential == null) {
        throw new CredentialsUnavailableException(id);
      }

      return credential;
    }

    return null;
  }

  public static InfisicalConfiguration pullAndMergeConfiguration(Run<?, ?> build,
      InfisicalConfiguration buildConfiguration) {
    InfisicalConfiguration configuration = buildConfiguration;
    for (InfisicalConfigResolver resolver : ExtensionList.lookup(InfisicalConfigResolver.class)) {
      if (configuration != null) {
        configuration = configuration
            .mergeWithParent(resolver.forJob(build.getParent()));
      } else {
        configuration = resolver.forJob(build.getParent());
      }
    }
    if (configuration == null) {
      throw new InfisicalPluginException(
          "No configuration found - please configure the Infisical Plugin .");
    }
    configuration.fixDefaults();

    return configuration;
  }
}
