package io.jenkins.plugins.infisicaljenkins.log;

import hudson.console.ConsoleLogFilter;
import hudson.model.Run;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jenkinsci.plugins.credentialsbinding.masking.SecretPatterns;

/*The logic in this class is borrowed from https://github.com/jenkinsci/credentials-binding-plugin/*/
public class MaskingConsoleLogFilter extends ConsoleLogFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String charsetName;
    private final List<String> valuesToMask;
    private Pattern pattern;
    private List<String> valuesToMaskInUse;

    public MaskingConsoleLogFilter(final String charsetName, List<String> valuesToMask) {
        this.charsetName = charsetName;
        this.valuesToMask = valuesToMask;
        updatePattern();
    }

    private synchronized Pattern updatePattern() {
        if (!valuesToMask.equals(valuesToMaskInUse)) {
            List<String> values = valuesToMask.stream().filter(Objects::nonNull).collect(Collectors.toList());
            pattern = values.isEmpty() ? null : SecretPatterns.getAggregateSecretPattern(values);
            valuesToMaskInUse = new ArrayList<>(valuesToMask);
        }
        return pattern;
    }

    @Override
    public OutputStream decorateLogger(@SuppressWarnings("rawtypes") Run run, final OutputStream logger)
            throws IOException, InterruptedException {
        return new SecretPatterns.MaskingOutputStream(logger, this::updatePattern, charsetName);
    }
}
