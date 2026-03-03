package io.jenkins.plugins.infisicaljenkins.infisical;

import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import io.jenkins.plugins.casc.SecretSource;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import io.jenkins.plugins.infisicaljenkins.credentials.InfisicalCredential;
import io.jenkins.plugins.infisicaljenkins.credentials.InfisicalLdapCredential;
import io.jenkins.plugins.infisicaljenkins.credentials.InfisicalUniversalAuthCredential;
import io.jenkins.plugins.infisicaljenkins.model.SingleSecretResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

@Extension(optional = true)
public class InfisicalSecretSource extends SecretSource {

    private static final Logger LOGGER = Logger.getLogger(InfisicalSecretSource.class.getName());

    // General Properties

    private static final String CASC_INFISICAL_ENVIRONMENT_SLUG = "CASC_INFISICAL_ENVIRONMENT_SLUG";
    private static final String CASC_INFISICAL_FILE = "CASC_INFISICAL_FILE";
    private static final String CASC_INFISICAL_INCLUDE_IMPORTS = "CASC_INFISICAL_INCLUDE_IMPORTS";
    private static final String CASC_INFISICAL_PROJECT_SLUG = "CASC_INFISICAL_PROJECT_SLUG";
    private static final String CASC_INFISICAL_PATHS = "CASC_INFISICAL_PATHS";
    private static final String CASC_INFISICAL_URL = "CASC_INFISICAL_URL";

    // Universal Authentication
    private static final String CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_ID = "CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_ID";
    private static final String CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_SECRET =
            "CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_SECRET";

    // LDAP Authentication
    private static final String CASC_INFISICAL_LDAP_IDENTITY_ID = "CASC_INFISICAL_LDAP_IDENTITY_ID";
    private static final String CASC_INFISICAL_LDAP_USER = "CASC_INFISICAL_LDAP_USER";
    private static final String CASC_INFISICAL_LDAP_PW = "CASC_INFISICAL_LDAP_PW";
    private static final String CASC_INFISICAL_RECURSIVE = "CASC_INFISICAL_RECURSIVE";

    private InfisicalCredential credential;
    private List<SingleSecretResponse> secrets;
    private InfisicalConfiguration configuration;
    private Properties prop;

    private static final String CREDENTIALS_ID = "LDAP_Credential_for_JCASC";

    public void init() {
        prop = new Properties();
        Optional<String> propsFile = Optional.ofNullable(System.getenv(CASC_INFISICAL_FILE));
        propsFile.ifPresent(this::readPropertiesFromFile);

        // LDAP properties
        Optional<String> ldapIdentityId = getVariable(CASC_INFISICAL_LDAP_IDENTITY_ID);
        Optional<String> ldapUser = getVariable(CASC_INFISICAL_LDAP_USER);
        Optional<String> ldapPw = getVariable(CASC_INFISICAL_LDAP_PW);

        // Universal Auth properties
        Optional<String> machineIdentityClientId = getVariable(CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_ID);
        Optional<String> machineIdentityClientSecret = getVariable(CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_SECRET);

        // General properties
        Optional<String> environmentSlug = getVariable(CASC_INFISICAL_ENVIRONMENT_SLUG);
        Optional<String> infisicalUrl = getVariable(CASC_INFISICAL_URL);
        Optional<String> includeImports = getVariable(CASC_INFISICAL_INCLUDE_IMPORTS);
        Optional<String> projectSlug = getVariable(CASC_INFISICAL_PROJECT_SLUG);
        Optional<String> recursive = getVariable(CASC_INFISICAL_RECURSIVE);
        String[] paths = getVariable(CASC_INFISICAL_PATHS).orElse("/").split(",");

        CredentialsScope scope = CredentialsScope.SYSTEM;

        credential = null;

        // Verify common properties are set
        if (environmentSlug.isEmpty() || projectSlug.isEmpty()) {
            LOGGER.info("Configuration missing.  Not using Infisical to perform JCasC variable lookup.");
            return;
        }

        // Determine credentials type
        if (ldapIdentityId.isPresent() && ldapUser.isPresent() && ldapPw.isPresent()) {
            LOGGER.info("Using LDAP credentials");
            credential = new InfisicalLdapCredential(
                    scope,
                    CREDENTIALS_ID,
                    "LDAP Credential for JCASC",
                    ldapIdentityId.get(),
                    ldapUser.get(),
                    ldapPw.get());
        } else if (machineIdentityClientId.isPresent() && machineIdentityClientSecret.isPresent()) {
            LOGGER.info("Using Universal Auth credentials");
            credential = new InfisicalUniversalAuthCredential(
                    scope,
                    CREDENTIALS_ID,
                    "LDAP Credential for JCASC",
                    machineIdentityClientId.get(),
                    machineIdentityClientSecret.get());
        } else {
            LOGGER.info("No credentials found.  Not using Infisical to perform JCasC variable lookup.");
            return;
        }

        configuration = new InfisicalConfiguration();
        configuration.setInfisicalCredentialId(CREDENTIALS_ID);
        configuration.setInfisicalUrl(infisicalUrl.orElse(null));
        configuration.setInfisicalProjectSlug(projectSlug.get());
        configuration.setInfisicalEnvironmentSlug(environmentSlug.get());

        retrieveSecrets(
                paths,
                Boolean.parseBoolean(includeImports.orElse("false")),
                Boolean.parseBoolean(recursive.orElse("false")));
    }

    private void retrieveSecrets(String[] paths, boolean includeImports, boolean recursive) {
        secrets = new ArrayList<>();

        for (String p : paths) {
            p = p.trim();

            PrintStream ps = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
                List<SingleSecretResponse> fetched =
                        InfisicalSecrets.getSecrets(configuration, credential, p, includeImports, recursive, ps);
                if (!fetched.isEmpty()) {
                    secrets.addAll(fetched);
                }
            } finally {
                if (ps != null) {
                    ps.flush();
                    LOGGER.info(baos.toString(StandardCharsets.UTF_8));
                    ps.close();
                }
            }
        }
    }

    private Optional<String> getVariable(String key) {
        return Optional.ofNullable(prop.getProperty(key, System.getenv(key)));
    }

    private void readPropertiesFromFile(String propsFile) {
        try (FileInputStream input = new FileInputStream(propsFile)) {
            prop.load(input);
            if (prop.isEmpty()) {
                LOGGER.log(Level.WARNING, "Infisical properties file is empty");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load Infisical properties from file", e);
        }
    }

    @Override
    public Optional<String> reveal(String secret) {
        if (StringUtils.isBlank(secret)) {
            return Optional.empty();
        }

        if (configuration == null) {
            return Optional.empty();
        }
        try {
            configuration.fixDefaults();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to prepare Infisical configuration for JCasC", e);
            return Optional.empty();
        }
        try {
            if (secrets == null || secrets.isEmpty()) {
                return Optional.empty();
            }

            for (SingleSecretResponse s : secrets) {
                if (secret.equals(s.getSecretKey())) {
                    return Optional.ofNullable(s.getSecretValue());
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to reveal Infisical secret for JCasC", e);
            return Optional.empty();
        }
    }
}
