package org.fever.utils;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FqnExtractor {
    public static final String FQN_REGEX = "^\"?@?[a-z0-9_.]+\\.[A-Z]+[a-zA-Z]+\"?$";
    private static final String YAML_FQN_GROUP_SELECTOR_REGEX = "fqn:\\s*(\\S+)";
    private static final String PYTHON_FQN_GROUP_SELECTOR_REGEX = "container_builder\\.set_definition\\(\\s+Definition\\(\\s*\"\\S+\",\\s*\"(\\S+)\"";

    private static @Nullable String extractGroupFromRegex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static @Nullable String extractFqnFromYaml(String fileContent) {
        return extractGroupFromRegex(fileContent, YAML_FQN_GROUP_SELECTOR_REGEX);
    }

    public static @Nullable String extractFqnFromPython(String fileContent) {
        return extractGroupFromRegex(fileContent, PYTHON_FQN_GROUP_SELECTOR_REGEX);
    }
}
