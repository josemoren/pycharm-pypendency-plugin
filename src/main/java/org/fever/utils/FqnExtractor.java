package org.fever.utils;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FqnExtractor {
    private static final String YAML_FQN_GROUP_SELECTOR_REGEX = "fqn:\\s*(\\S+)";
    private static final String PYTHON_FQN_GROUP_SELECTOR_REGEX = "container_builder\\.set_definition\\(\\s+Definition\\(\\s*\"\\S+\",\\s*\"(\\S+)\"";

    private static @Nullable String extractGroupFromRegex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static @Nullable String extractFqnFromDIFile(@Nullable PsiFile dependencyInjectionFile) {
        if (dependencyInjectionFile == null) {
            return null;
        }
        String extension = dependencyInjectionFile.getVirtualFile().getExtension();
        if (extension == null) {
            return null;
        }

        String fileContent = dependencyInjectionFile.getText();
        if (isYamlFile(extension)) {
            return FqnExtractor.extractFqnFromYaml(fileContent);
        }
        if (isPythonFile(extension)) {
            return FqnExtractor.extractFqnFromPython(fileContent);
        }
        return null;
    }

    private static Boolean isYamlFile(String extension) {
        return extension.equals("yaml") || extension.equals("yml");
    }

    private static Boolean isPythonFile(String extension) {
        return extension.equals("py");
    }


    public static @Nullable String extractFqnFromYaml(String fileContent) {
        return extractGroupFromRegex(fileContent, YAML_FQN_GROUP_SELECTOR_REGEX);
    }

    public static @Nullable String extractFqnFromPython(String fileContent) {
        return extractGroupFromRegex(fileContent, PYTHON_FQN_GROUP_SELECTOR_REGEX);
    }
}
