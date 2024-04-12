package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.PythonFileType;

public class PythonFileCreator {
    private static final String PYTHON_DI_FILE_CONTENT_TEMPLATE = """
            from pypendency.argument import Argument
            from pypendency.builder import ContainerBuilder
            from pypendency.definition import Definition
            
            from django.conf import settings
            
            
            def load(container_builder: ContainerBuilder) -> None:
                container_builder.set_definition(
                    Definition(
                        "{fqn}",
                        "{fqn}",
                        [
                            {arguments},
                        ],
                    )
                )
                    """;

    public static PsiFile create(PsiFile sourceCodeFile, String fqn) {
        String baseContent = PYTHON_DI_FILE_CONTENT_TEMPLATE.replace("{fqn}", fqn);
        String contentWithArguments = baseContent.replace("{arguments}", getArguments());

        return PsiFileFactory.getInstance(sourceCodeFile.getProject()).createFileFromText(
                sourceCodeFile.getName(),
                PythonFileType.INSTANCE,
                contentWithArguments
        );
    }

    private static String getArguments() {
        return "Argument.no_kw_argument()";
    }
}
