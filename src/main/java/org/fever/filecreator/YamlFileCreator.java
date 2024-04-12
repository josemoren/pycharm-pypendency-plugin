package org.fever.filecreator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.yaml.YAMLFileType;

public class YamlFileCreator {
    private static final String YAML_DI_FILE_CONTENT_TEMPLATE = """
            {fqn}:
                fqn: {fqn}
                {arguments}
            """;

    public static PsiFile create(Project project, String fileName, String fqn) {
        String baseContent = YAML_DI_FILE_CONTENT_TEMPLATE.replace("{fqn}", fqn);
        String contentWithArguments = baseContent.replace("{arguments}", getArguments());

        return PsiFileFactory.getInstance(project).createFileFromText(
                fileName,
                YAMLFileType.YML,
                contentWithArguments
        );
    }

    private static String getArguments() {
        return "";
    }
}
