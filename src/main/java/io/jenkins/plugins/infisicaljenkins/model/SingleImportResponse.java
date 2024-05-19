package io.jenkins.plugins.infisicaljenkins.model;

import java.util.List;

public class SingleImportResponse {

    private String secretPath;
    private String environment;
    private String folderId;
    private List<SingleSecretResponse> secrets;

    public SingleImportResponse(
            String secretPath, String environment, String folderId, List<SingleSecretResponse> secrets) {
        this.secretPath = secretPath;
        this.environment = environment;
        this.folderId = folderId;
        this.secrets = secrets;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getFolderId() {
        return folderId;
    }

    public List<SingleSecretResponse> getSecrets() {
        return secrets;
    }
}
