package io.jenkins.plugins.infisicaljenkins.infisical;

import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

public class InfisicalAuth implements Serializable {

    public String loginWithLdapAuth(String infisicalUrl, String identityId, String username, String password) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(infisicalUrl + "/api/v1/auth/ldap-auth/login");
            connection = (HttpsURLConnection) url.openConnection();

            // Set request method to POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Set headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("identityId", identityId);
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            String jsonInputString = jsonBody.toString();

            // Send request
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
            }

            String accessToken = getTokenFromConnection(connection);
            return String.format("Bearer %s", accessToken);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InfisicalPluginException(ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String loginWithUniversalAuth(
            String infisicalUrl, String machineIdentityClientId, String machineIdentityClientSecret) {
        HttpsURLConnection connection = null;
        try {
            // Create the URL, and append "/api/v1/auth/universal-auth/login" to it
            URL url = new URL(infisicalUrl + "/api/v1/auth/universal-auth/login");
            connection = (HttpsURLConnection) url.openConnection();

            // Set request method to POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Set headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Create JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("clientId", machineIdentityClientId);
            jsonBody.put("clientSecret", machineIdentityClientSecret);
            String jsonInputString = jsonBody.toString();

            // Send request
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
            }

            String accessToken = getTokenFromConnection(connection);
            return String.format("Bearer %s", accessToken);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InfisicalPluginException(ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getTokenFromConnection(HttpURLConnection connection) throws InfisicalPluginException, IOException {
        // Check response code
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the response using org.json
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("accessToken"); // Extract the access token
        } else {
            throw new InfisicalPluginException("Failed to authenticate with Infisical. Response code: " + responseCode);
        }
    }

    public void revoke(String infisicalUrl, String token) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(infisicalUrl + "/api/v1/auth/token/revoke");
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InfisicalPluginException(ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
