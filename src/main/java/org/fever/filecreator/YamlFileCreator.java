package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.yaml.YAMLFileType;

public class YamlFileCreator {
    private static final String YAML_DI_FILE_CONTENT_TEMPLATE = """
            {fqn}:
                fqn: {fqn}
            """;

    public static PsiFile create(PsiFile sourceCodeFile, String fqn) {
        String baseContent = YAML_DI_FILE_CONTENT_TEMPLATE.replace("{fqn}", fqn);
        String contentWithArguments = baseContent.concat(getArguments());

        return PsiFileFactory.getInstance(sourceCodeFile.getProject()).createFileFromText(
                sourceCodeFile.getName().replace(".py", ".yaml"),
                YAMLFileType.YML,
                contentWithArguments
        );
    }

    private static String getArguments() {
        return "";
    }
}
