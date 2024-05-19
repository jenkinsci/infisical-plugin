package io.jenkins.plugins.infisicaljenkins.model;

import java.util.List;

public class SecretResponseWrapper {
    List<SingleSecretResponse> secrets;
    List<SingleImportResponse> imports;

    SecretResponseWrapper(List<SingleSecretResponse> secrets, List<SingleImportResponse> imports) {
        this.secrets = secrets;
        this.imports = imports;
    }

    public List<SingleSecretResponse> getSecrets() {
        return secrets;
    }

    public List<SingleImportResponse> getImports() {
        return imports;
    }
}
