package org.fever.utils;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierExtractor {
    private static final String YAML_IDENTIFIER_GROUP_SELECTOR_REGEX = "^(\\S+):";
    private static final String PYTHON_IDENTIFIER_GROUP_SELECTOR_REGEX = "container_builder\\.set_definition\\(\\s+Definition\\(\\s*\"(\\S+)\",\\s*\"\\S+\"";
    private static final String PYTHON_IDENTIFIER_GROUP_SELECTOR_DEFINED_MANUALLY_REGEX = "container(?:_builder)?\\.set\\(\\s*\"(\\S+)\"";
    private static final List<String> EMPTY_LIST = List.of();

    private static @Nullable List<String> extractGroupMatchesFromRegex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        List<String> allGroupMatches = new ArrayList<>();

        while (matcher.find()) {
            allGroupMatches.add(matcher.group(1));
        }

        return allGroupMatches.isEmpty() ? null : allGroupMatches;
    }

    public static List<String> extractIdentifiersFromDIFile(@Nullable PsiFile dependencyInjectionFile) {
        if (dependencyInjectionFile == null) {
            return EMPTY_LIST;
        }

        String extension = dependencyInjectionFile.getVirtualFile().getExtension();
        if (extension == null) {
            return EMPTY_LIST;
        }

        String fileContent = dependencyInjectionFile.getText();
        if (isYamlFile(extension)) {
            return extractIdentifiersFromYaml(fileContent);
        }

        if (isPythonFile(extension)) {
            return extractIdentifiersFromPython(fileContent);
        }

        return EMPTY_LIST;
    }

    public static Boolean isYamlFile(String extension) {
        return extension.equals("yaml") || extension.equals("yml");
    }

    public static Boolean isPythonFile(String extension) {
        return extension.equals("py");
    }

    public static @Nullable List<String> extractIdentifiersFromYaml(String fileContent) {
        return extractGroupMatchesFromRegex(fileContent, YAML_IDENTIFIER_GROUP_SELECTOR_REGEX);
    }

    public static @Nullable List<String> extractIdentifiersFromPython(String fileContent) {
        for (String regex : new String[]{ PYTHON_IDENTIFIER_GROUP_SELECTOR_REGEX, PYTHON_IDENTIFIER_GROUP_SELECTOR_DEFINED_MANUALLY_REGEX }) {
            List<String> identifiers = extractGroupMatchesFromRegex(fileContent, regex);

            if (identifiers != null) {
                return identifiers;
            }
        }

        return null;
    }
}
