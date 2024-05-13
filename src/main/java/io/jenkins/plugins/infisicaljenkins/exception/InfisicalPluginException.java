package io.jenkins.plugins.infisicaljenkins.exception;

public class InfisicalPluginException extends RuntimeException {

    public InfisicalPluginException(String message) {
        super(message);
    }

    public InfisicalPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
