package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

public abstract class AbstractInfisicalUniversalAuthCredential
    extends BaseStandardCredentials implements InfisicalCredential {

  protected AbstractInfisicalUniversalAuthCredential(CredentialsScope scope, String id, String description) {
    super(scope, id, description);
  }
}
