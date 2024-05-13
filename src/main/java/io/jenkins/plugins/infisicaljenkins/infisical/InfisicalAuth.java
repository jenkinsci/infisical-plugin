package io.jenkins.plugins.infisicaljenkins.infisical;

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

import io.jenkins.plugins.infisicaljenkins.exception.InfisicalPluginException;

public class InfisicalAuth implements Serializable {

  public String loginWithUniversalAuth(String infisicalUrl, String machineIdentityClientId,
      String machineIdentityClientSecret) {
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

      // Check response code
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) { // success
        BufferedReader in = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        // Parse the response using org.json
        JSONObject jsonResponse = new JSONObject(response.toString());
        String accessToken = jsonResponse.getString("accessToken"); // Extract the access token

        return String.format("Bearer %s", accessToken);
      } else {
        throw new InfisicalPluginException("Failed to authenticate with Infisical. Response code: " + responseCode);
      }
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
