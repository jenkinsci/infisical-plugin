package io.jenkins.plugins.infisicaljenkins.credentials.common;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.ItemGroup;
import org.kohsuke.stapler.DataBoundSetter;

public abstract class AbstractInfisicalBaseStandardCredentials extends BaseStandardCredentials {

    private String path;
    private String prefixPath;
    private String namespace;
    private Integer engineVersion;

    @SuppressWarnings("rawtypes")
    private transient ItemGroup context;

    AbstractInfisicalBaseStandardCredentials(CredentialsScope scope, String id, String description) {
        super(scope, id, description);
    }

    @NonNull
    public String getPrefixPath() {
        return prefixPath;
    }

    @DataBoundSetter
    public void setPrefixPath(String prefixPath) {
        this.prefixPath = Util.fixEmptyAndTrim(prefixPath);
    }

    @NonNull
    public String getPath() {
        return path;
    }

    @DataBoundSetter
    public void setPath(String path) {
        this.path = path;
    }

    @CheckForNull
    public String getNamespace() {
        return namespace;
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = Util.fixEmptyAndTrim(namespace);
    }

    @CheckForNull
    public Integer getEngineVersion() {
        return engineVersion;
    }

    @DataBoundSetter
    public void setEngineVersion(Integer engineVersion) {
        this.engineVersion = engineVersion;
    }

    public void setContext(@SuppressWarnings("rawtypes") @NonNull ItemGroup context) {
        this.context = context;
    }

    @SuppressWarnings("rawtypes")
    public ItemGroup getContext() {
        return this.context;
    }

    /**
     * Get credential display name. Defaults to secret path.
     *
     * @return display name
     */
    public String getDisplayName() {
        return this.path;
    }
}
