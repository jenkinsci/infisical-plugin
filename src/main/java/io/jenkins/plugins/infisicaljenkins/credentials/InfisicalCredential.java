package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import java.io.Serializable;

@SuppressWarnings("unused")
@NameWith(InfisicalCredential.NameProvider.class)
public interface InfisicalCredential extends StandardCredentials, Serializable {

    String getAccessToken(InfisicalConfiguration configuration);

    class NameProvider extends CredentialsNameProvider<InfisicalCredential> {

        @NonNull
        public String getName(@NonNull InfisicalCredential credentials) {
            return credentials.getDescription();
        }
    }
}
