package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PythonFileCreator {
    private static final String ARGUMENT_INDENTATION = " ".repeat(16);
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
                        [{arguments}],
                    )
                )
                    """;

    public static PsiFile create(PyFile sourceCodeFile, String fqn) {
        String baseContent = PYTHON_DI_FILE_CONTENT_TEMPLATE.replace("{fqn}", fqn);
        String contentWithArguments = baseContent.replace("{arguments}", getArguments(sourceCodeFile));

        return PsiFileFactory.getInstance(sourceCodeFile.getProject()).createFileFromText(
                sourceCodeFile.getName(),
                PythonFileType.INSTANCE,
                contentWithArguments
        );
    }

    private static String getArguments(PyFile sourceCodeFile) {
        Collection<FQNItem> initArguments = ClassArgumentFetcher.getFqnOfInitArguments(sourceCodeFile);
        if (initArguments.isEmpty()) {
            return "";
        }

        StringBuilder arguments = new StringBuilder();

        for (FQNItem initArgument : initArguments) {
            arguments.append("\n").append(ARGUMENT_INDENTATION);
            if (initArgument.isNotAClass()) {
                arguments.append(noKwArgumentBuilder(null, initArgument.message));
            } else if (initArgument.hasNoImplementations()) {
                arguments.append(noKwArgumentBuilder("No implementation found for " + initArgument.parentClass.getName(), null));
            } else {
                arguments.append(noKwArgumentBuilder(initArgument.fqn, null));
            }
            if (FileCreator.countImplementations(initArgument, initArguments) > 1) {
                String message = "TODO: multiple implementations found for " + initArgument.parentClass.getName() + ", leave only one";
                arguments.append(noKwArgumentBuilder(initArgument.fqn, message));
            }
        }
        arguments.append("\n").append(" ".repeat(12));
        return arguments.toString();
        }

    private static String noKwArgumentBuilder(String argument, @Nullable String message) {
        String result = "";
        if (argument != null) {
            result += "Argument.no_kw_argument(\"" + argument + "\"),";
        }
        if (message != null) {
            result += "  # " + message;
        }
        return result;
    }


}
