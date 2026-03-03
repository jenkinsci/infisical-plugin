package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import io.jenkins.plugins.infisicaljenkins.infisical.InfisicalAuth;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class InfisicalLdapCredential extends AbstractAuthenticatingInfisicalTokenCredential {

    @NonNull
    private String identityId;

    @NonNull
    private String username;

    @NonNull
    private String password;

    private final InfisicalAuth infisicalAuth;

    @DataBoundConstructor
    public InfisicalLdapCredential(
            @CheckForNull CredentialsScope scope,
            @CheckForNull String id,
            @CheckForNull String description,
            @NonNull String identityId,
            @NonNull String username,
            @NonNull String password) {
        super(scope, id, description);
        this.infisicalAuth = new InfisicalAuth();
        this.identityId = identityId;
        this.username = username;
        this.password = password;
    }

    @NonNull
    public String getIdentityId() {
        return identityId;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @DataBoundSetter
    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    @DataBoundSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessToken(InfisicalConfiguration configuration) {
        try {
            return infisicalAuth.loginWithLdapAuth(configuration.getInfisicalUrl(), identityId, username, password);

        } catch (InfisicalPluginException e) {
            throw new InfisicalPluginException("Failed to authenticate with Infisical", e);
        }
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Infisical LDAP Credential";
        }
    }
}
