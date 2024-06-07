package org.fever.utils;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierExtractor {
    private static final String YAML_IDENTIFIER_GROUP_SELECTOR_REGEX = "^(\\S+):";
    private static final String PYTHON_IDENTIFIER_GROUP_SELECTOR_REGEX = "container_builder\\.set_definition\\(\\s+Definition\\(\\s*\"(\\S+)\",\\s*\"\\S+\"";
    private static final String PYTHON_IDENTIFIER_GROUP_SELECTOR_DEFINED_MANUALLY_REGEX = "container(?:_builder)?\\.set\\(\\s*\"(\\S+)\"";

    private static @Nullable String extractGroupFromRegex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static @Nullable String extractIdentifierFromDIFile(@Nullable PsiFile dependencyInjectionFile) {
        if (dependencyInjectionFile == null) {
            return null;
        }
        String extension = dependencyInjectionFile.getVirtualFile().getExtension();
        if (extension == null) {
            return null;
        }

        String fileContent = dependencyInjectionFile.getText();
        if (isYamlFile(extension)) {
            return extractIdentifierFromYaml(fileContent);
        }
        if (isPythonFile(extension)) {
            return extractIdentifierFromPython(fileContent);
        }
        return null;
    }

    public static Boolean isYamlFile(String extension) {
        return extension.equals("yaml") || extension.equals("yml");
    }

    public static Boolean isPythonFile(String extension) {
        return extension.equals("py");
    }


    public static @Nullable String extractIdentifierFromYaml(String fileContent) {
        return extractGroupFromRegex(fileContent, YAML_IDENTIFIER_GROUP_SELECTOR_REGEX);
    }

    public static @Nullable String extractIdentifierFromPython(String fileContent) {
        for (String regex : new String[]{PYTHON_IDENTIFIER_GROUP_SELECTOR_REGEX, PYTHON_IDENTIFIER_GROUP_SELECTOR_DEFINED_MANUALLY_REGEX}) {
            String identifier = extractGroupFromRegex(fileContent, regex);
            if (identifier != null) {
                return identifier;
            }
        }
        return null;
    }
}
