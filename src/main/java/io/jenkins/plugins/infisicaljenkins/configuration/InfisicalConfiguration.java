package io.jenkins.plugins.infisicaljenkins.configuration;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.infisicaljenkins.credentials.InfisicalCredential;
import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class InfisicalConfiguration extends AbstractDescribableImpl<InfisicalConfiguration> implements Serializable {

    private String infisicalUrl;
    private String infisicalCredentialId;
    private String infisicalProjectSlug;
    private String infisicalEnvironmentSlug;

    @DataBoundConstructor
    public InfisicalConfiguration() {
        // no args constructor
    }

    @Deprecated
    public InfisicalConfiguration(
            String infisicalUrl,
            String infisicalCredentialId,
            String infisicalProjectSlug,
            String infisicalEnvironmentSlug) {
        setInfisicalUrl(infisicalUrl);
        setInfisicalCredentialId(infisicalCredentialId);
        setInfisicalProjectSlug(infisicalProjectSlug);
        setInfisicalEnvironmentSlug(infisicalEnvironmentSlug);
    }

    public InfisicalConfiguration(InfisicalConfiguration toCopy) {
        this.infisicalUrl = toCopy.getInfisicalUrl();
        this.infisicalProjectSlug = toCopy.getInfisicalProjectSlug();
        this.infisicalEnvironmentSlug = toCopy.getInfisicalEnvironmentSlug();
    }

    public InfisicalConfiguration fixDefaults() {
        if (getInfisicalUrl() == null) {
            setInfisicalUrl(DescriptorImpl.DEFAULT_INFISICAL_URL);
        }

        if (getInfisicalCredentialId() == null) {
            throw new InfisicalPluginException("Please select an Infisical Credential to authenticate with");
        }

        // check if any of the fields are null, and if they are, throw an exception
        if (getInfisicalProjectSlug() == null) {
            throw new InfisicalPluginException("Infisical Project Slug is required");
        } else if (getInfisicalEnvironmentSlug() == null) {
            throw new InfisicalPluginException("Infisical Environment Slug is required");
        }

        return this;
    }

    public InfisicalConfiguration mergeWithParent(InfisicalConfiguration parent) {
        if (parent == null) {
            return this;
        }
        InfisicalConfiguration result = new InfisicalConfiguration(this);
        if (StringUtils.isBlank(result.getInfisicalEnvironmentSlug())) {
            result.setInfisicalEnvironmentSlug(parent.getInfisicalEnvironmentSlug());
        }
        if (StringUtils.isBlank(result.getInfisicalProjectSlug())) {
            result.setInfisicalProjectSlug(parent.getInfisicalProjectSlug());
        }
        if (StringUtils.isBlank(result.getInfisicalUrl())) {
            result.setInfisicalUrl(parent.getInfisicalUrl());
        }

        return result;
    }

    public String getInfisicalUrl() {
        return infisicalUrl;
    }

    public String getInfisicalCredentialId() {
        return infisicalCredentialId;
    }

    public String getInfisicalProjectSlug() {
        return infisicalProjectSlug;
    }

    public String getInfisicalEnvironmentSlug() {
        return infisicalEnvironmentSlug;
    }

    @DataBoundSetter
    public void setInfisicalUrl(String infisicalUrl) {
        this.infisicalUrl = infisicalUrl;
    }

    @DataBoundSetter
    public void setInfisicalCredentialId(String infisicalCredentialId) {
        this.infisicalCredentialId = infisicalCredentialId;
    }

    @DataBoundSetter
    public void setInfisicalProjectSlug(String infisicalProjectSlug) {
        this.infisicalProjectSlug = infisicalProjectSlug;
    }

    @DataBoundSetter
    public void setInfisicalEnvironmentSlug(String infisicalEnvironmentSlug) {
        this.infisicalEnvironmentSlug = infisicalEnvironmentSlug;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<InfisicalConfiguration> {

        public static final String DEFAULT_INFISICAL_URL = "https://app.infisical.com";

        @Override
        @NonNull
        public String getDisplayName() {
            return "Infisical Configuration";
        }

        @SuppressWarnings({
            "unused",
            "deprecation",
            "lgtm[jenkins/credentials-fill-without-permission-check]",
            "lgtm[jenkins/csrf]",
            "lgtm[jenkins/no-permission-check]"
        })
        public ListBoxModel doFillInfisicalCredentialIdItems(@AncestorInPath Item item, @QueryParameter String uri) {
            // This is needed for folders: credentials bound to a folder are
            // realized through domain requirements
            List<DomainRequirement> domainRequirements =
                    URIRequirementBuilder.fromUri(uri).build();
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeAs(ACL.SYSTEM, item, InfisicalCredential.class, domainRequirements);
        }
    }
}
