package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.psi.*;
import org.jetbrains.yaml.YAMLFileType;

import java.util.Collection;

public class YamlFileCreator implements FileCreator {
    private static final String YAML_DI_FILE_CONTENT_TEMPLATE = """
            {identifier}:
                fqn: {fqn}
            """;

    public static PsiFile create(PyFile sourceCodeFile, String identifier) {
        String baseContent = YAML_DI_FILE_CONTENT_TEMPLATE
                .replace("{identifier}", identifier)
                .replace("{fqn}", identifier);
        String contentWithArguments = baseContent.concat(getArguments(sourceCodeFile));

        return PsiFileFactory.getInstance(sourceCodeFile.getProject()).createFileFromText(
                sourceCodeFile.getName().replace(".py", ".yaml"),
                YAMLFileType.YML,
                contentWithArguments
        );
    }

    private static String getArguments(PyFile sourceCodeFile) {
        Collection<IdentifierItem> initArguments = ClassArgumentFetcher.getFqnOfInitArguments(sourceCodeFile);
        if (initArguments.isEmpty()) {
            return "";
        }

        StringBuilder arguments = new StringBuilder("    args:");

        for (IdentifierItem initArgument : initArguments) {
            arguments.append("\n        - ");
            if (initArgument.isNotAClass()) {
                arguments.append("\"").append(initArgument.message).append("\" # TODO: incomplete resolution");
            } else if (initArgument.hasNoImplementations()) {
                arguments.append("\"No implementation found for ").append(initArgument.parentClass.getName()).append("\"");
            } else {
                arguments.append("\"@").append(initArgument.identifier).append("\"");
            }
            if (FileCreator.countImplementations(initArgument, initArguments) > 1) {
                arguments.append(" # TODO: multiple implementations found for ").append(initArgument.parentClass.getName()).append(", leave only one");
            }
        }

        return arguments.toString();
    }
}
