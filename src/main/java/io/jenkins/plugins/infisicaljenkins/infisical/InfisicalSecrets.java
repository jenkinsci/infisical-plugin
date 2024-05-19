package io.jenkins.plugins.infisicaljenkins.infisical;

import com.google.gson.Gson;
import io.jenkins.plugins.infisicaljenkins.configuration.InfisicalConfiguration;
import io.jenkins.plugins.infisicaljenkins.credentials.InfisicalCredential;
import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import io.jenkins.plugins.infisicaljenkins.model.SecretResponseWrapper;
import io.jenkins.plugins.infisicaljenkins.model.SingleImportResponse;
import io.jenkins.plugins.infisicaljenkins.model.SingleSecretResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public class InfisicalSecrets implements Serializable {

    public static List<SingleSecretResponse> getSecrets(
            InfisicalConfiguration configuration,
            InfisicalCredential credential,
            String secretPath,
            boolean includeImports,
            PrintStream logger) {

        String accessToken;
        HttpsURLConnection connection = null;

        try {
            accessToken = credential.getAccessToken(configuration);
        } catch (InfisicalPluginException e) {
            throw new InfisicalPluginException("Failed to authenticate with Infisical", e);
        }

        try {
            // Updated to include the secretPath as a query parameter
            String urlString = String.format(
                    "%s%s?secretPath=%s&workspaceSlug=%s&environment=%s&expandSecretReferences=true&include_imports=%s",
                    configuration.getInfisicalUrl(),
                    "/api/v3/secrets/raw",
                    URLEncoder.encode(secretPath, "UTF-8"),
                    configuration.getInfisicalProjectSlug(),
                    configuration.getInfisicalEnvironmentSlug(),
                    includeImports ? "true" : "false");

            logger.println("Fetching secrets from Infisical at: " + urlString);

            URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Set headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", accessToken); // Include the accessToken in auth header

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // HTTP_OK is 200
                // Read the response using Gson
                Gson gson = new Gson();
                SecretResponseWrapper response = gson.fromJson(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8),
                        SecretResponseWrapper.class);

                if (response == null) {
                    throw new InfisicalPluginException("Failed to fetch secrets from Infisical, got null response");
                }

                List<SingleImportResponse> imports = response.getImports();
                List<SingleSecretResponse> secrets = response.getSecrets();

                if (secrets == null || (includeImports && imports == null)) {
                    throw new InfisicalPluginException(
                            "Failed to fetch secrets from Infisical, secrets and/or imports are null");
                }

                if (includeImports && imports != null && !imports.isEmpty()) {

                    imports.forEach(importedSecret -> {
                        try {
                            List<SingleSecretResponse> importedSecrets = importedSecret.getSecrets();

                            // We need to make sure the response.secrets only has one key
                            // with the same
                            // name. In other terms, the keys should be unique.
                            // The imported secrets should take precedence over the existing
                            // secrets.
                            importedSecrets.forEach(importedSecretResponse -> {
                                if (secrets.stream().anyMatch(secret -> secret.getSecretKey()
                                        .equals(importedSecretResponse.getSecretKey()))) {
                                    secrets.removeIf(secret ->
                                            secret.getSecretKey().equals(importedSecretResponse.getSecretKey()));
                                }

                                secrets.add(importedSecretResponse);
                            });

                        } catch (InfisicalPluginException e) {
                            throw new InfisicalPluginException("Failed to fetch imported secrets from Infisical", e);
                        }
                    });
                }

                return secrets;

            } else {
                throw new InfisicalPluginException("Failed to fetch secrets. HTTP error code: " + responseCode);
            }

        } catch (IOException ex) {
            throw new InfisicalPluginException("Failed to read secrets from Infisical", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
