package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import io.jenkins.plugins.infisicaljenkins.infisical.InfisicalAuth;
import org.kohsuke.stapler.DataBoundConstructor;

public class InfisicalUniversalAuthCredential extends AbstractAuthenticatingInfisicalTokenCredential {

    @NonNull
    private final String machineIdentityClientId;

    @NonNull
    private final String machineIdentityClientSecret;

    private final InfisicalAuth infisicalAuth;

    @DataBoundConstructor
    public InfisicalUniversalAuthCredential(
            @CheckForNull CredentialsScope scope,
            @CheckForNull String id,
            @CheckForNull String description,
            @NonNull String machineIdentityClientId,
            @NonNull String machineIdentityClientSecret) {
        super(scope, id, description);
        this.infisicalAuth = new InfisicalAuth();
        this.machineIdentityClientId = machineIdentityClientId;
        this.machineIdentityClientSecret = machineIdentityClientSecret;
    }

    @NonNull
    public String getMachineIdentityClientId() {
        return machineIdentityClientId;
    }

    @NonNull
    public String getMachineIdentityClientSecret() {
        return machineIdentityClientSecret;
    }

    public String getAccessToken(InfisicalConfiguration configuration) {
        try {
            return infisicalAuth.loginWithUniversalAuth(
                    configuration.getInfisicalUrl(), machineIdentityClientId, machineIdentityClientSecret);

        } catch (InfisicalPluginException e) {
            throw new InfisicalPluginException("Failed to authenticate with Infisical", e);
        }
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Infisical Universal Auth Credential";
        }
    }
}
