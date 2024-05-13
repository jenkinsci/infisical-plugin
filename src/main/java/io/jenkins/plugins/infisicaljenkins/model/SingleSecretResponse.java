package io.jenkins.plugins.infisicaljenkins.model;

public class SingleSecretResponse {

  private String secretPath;

  private String id;

  @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
  private String secretKey;

  private String secretValue;

  public SingleSecretResponse(String secretKey, String secretValue, String secretPath, String id) {
    this.secretPath = secretPath;
    this.id = id;
    this.secretKey = secretKey;
    this.secretValue = secretValue;
  }

  public String getSecretPath() {
    return secretPath;
  }

  public String getId() {
    return id;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getSecretValue() {
    return secretValue;
  }
}
