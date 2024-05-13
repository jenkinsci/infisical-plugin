package io.jenkins.plugins.infisicaljenkins.credentials;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import io.jenkins.plugins.infisicaljenkins.credentials.common.AbstractInfisicalBaseStandardCredentials;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

/**
 * This class provides the credentials that we need to authenticate against
 * Infisical
 * and the credentials stored in Infisical, after assigning the right context to
 * them.
 */
@SuppressWarnings("deprecation")
@Extension(optional = true, ordinal = 1)
public class InfisicalCredentialsProvider extends CredentialsProvider {

  @Override
  public <C extends Credentials> List<C> getCredentials(Class<C> type,
      @SuppressWarnings("rawtypes") ItemGroup itemGroup,
      Authentication authentication) {
    return getCredentials(type, itemGroup, authentication,
        Collections.emptyList());
  }

  @NonNull
  @Override
  public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type,
      @SuppressWarnings("rawtypes") @Nullable ItemGroup itemGroup,
      @Nullable Authentication authentication,
      @NonNull List<DomainRequirement> domainRequirements) {
    CredentialsMatcher matcher = (type != InfisicalCredential.class
        ? CredentialsMatchers.instanceOf(AbstractInfisicalBaseStandardCredentials.class)
        : CredentialsMatchers.always());
    List<C> creds = new ArrayList<C>();
    if (ACL.SYSTEM.equals(authentication)) {
      for (@SuppressWarnings("rawtypes")
      ItemGroup g = itemGroup; g instanceof AbstractFolder; g = (AbstractFolder.class.cast(g)).getParent()) {
        FolderCredentialsProperty property = ((AbstractFolder<?>) g).getProperties()
            .get(FolderCredentialsProperty.class);
        if (property == null) {
          continue;
        }

        List<C> folderCreds = DomainCredentials.getCredentials(
            property.getDomainCredentialsMap(),
            type,
            domainRequirements,
            matcher);

        if (type != InfisicalCredential.class) {
          for (C c : folderCreds) {
            ((AbstractInfisicalBaseStandardCredentials) c).setContext(g);
          }
        }

        creds.addAll(folderCreds);
      }

      List<C> globalCreds = DomainCredentials.getCredentials(
          SystemCredentialsProvider.getInstance().getDomainCredentialsMap(),
          type,
          domainRequirements,
          matcher);
      if (type != InfisicalCredential.class) {
        for (C c : globalCreds) {
          ((AbstractInfisicalBaseStandardCredentials) c).setContext(Jenkins.get());
        }
      }
      creds.addAll(globalCreds);
    }

    return creds;
  }
}
