package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

public abstract class AbstractInfisicalTokenCredential
    extends BaseStandardCredentials implements InfisicalCredential {

  protected AbstractInfisicalTokenCredential(CredentialsScope scope, String id, String description) {
    super(scope, id, description);
  }
}
