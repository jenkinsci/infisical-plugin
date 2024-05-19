package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import java.util.logging.Logger;

public abstract class AbstractInfisicalTokenCredentialWithExpiration extends AbstractInfisicalUniversalAuthCredential {

    protected static final Logger LOGGER =
            Logger.getLogger(AbstractInfisicalTokenCredentialWithExpiration.class.getName());

    protected AbstractInfisicalTokenCredentialWithExpiration(CredentialsScope scope, String id, String description) {
        super(scope, id, description);
    }
}
