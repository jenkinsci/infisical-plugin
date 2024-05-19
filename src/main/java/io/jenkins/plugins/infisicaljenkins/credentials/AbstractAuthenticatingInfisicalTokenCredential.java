package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;

/**
 * Abstract Infisical token credential that authenticates with the Infisical server to retrieve the
 * authentication token. This credential type can explicitly configure the namespace which the
 * authentication method is mounted.
 */
public abstract class AbstractAuthenticatingInfisicalTokenCredential
        extends AbstractInfisicalTokenCredentialWithExpiration {

    protected AbstractAuthenticatingInfisicalTokenCredential(CredentialsScope scope, String id, String description) {
        super(scope, id, description);
    }
}
