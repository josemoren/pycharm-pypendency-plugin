package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyParameter;
import groovyjarjarantlr4.v4.misc.OrderedHashMap;
import org.fever.filecreator.templates.DIFileTemplate;
import org.fever.filecreator.templates.PythonDIFileTemplate;
import org.fever.filecreator.templates.YamlDIFileTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DIFileCreator {

    public static PsiFile create(PyFile sourceCodeFile, String identifier, DIFileType type) {
        DIFileTemplate fileTemplate = getDIFileTemplate(type);
        String baseContent = fileTemplate.getBaseTemplate()
                .replace("{identifier}", identifier)
                .replace("{fqn}", identifier);
        String contentWithArguments = baseContent.concat(getArguments(sourceCodeFile, fileTemplate));

        return PsiFileFactory.getInstance(sourceCodeFile.getProject()).createFileFromText(
                sourceCodeFile.getName().replace(".py", fileTemplate.getFileExtension()),
                fileTemplate.getFileType(),
                contentWithArguments
        );
    }

    private static DIFileTemplate getDIFileTemplate(DIFileType type) {
        if (type == DIFileType.PYTHON) return new PythonDIFileTemplate();
        if (type == DIFileType.YAML) return new YamlDIFileTemplate();
        throw new IllegalArgumentException("Unknown DIFileType: " + type);
    }

    private static String getArguments(PyFile sourceCodeFile, DIFileTemplate fileTemplate) {
        Collection<IdentifierItem> initArguments = ClassArgumentFetcher.getFqnOfInitArguments(sourceCodeFile);
        if (initArguments.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(fileTemplate.getArgumentStatementBeginning());
        int numberOfSpaces = fileTemplate.getArgumentIndentationSpaces();

        for (Map.Entry<PyParameter, List<IdentifierItem>> entry : groupIdentifiersByParameter(initArguments).entrySet()) {
            PyParameter parameter = entry.getKey();
            List<IdentifierItem> initArgumentsForParameter = entry.getValue();

            if (initArgumentsForParameter.size() > 1) {
                appendWithIndentation(builder, numberOfSpaces, fileTemplate.getMultipleArgumentsTemplate().formatted(parameter.getName()));
            }
            for (IdentifierItem initArgument : initArgumentsForParameter) {
                if (initArgument.isNotAClass() || (initArgument.hasNoImplementations() && initArgumentsForParameter.size() == 1)) {
                    appendWithIndentation(builder, numberOfSpaces, fileTemplate.getMissingArgumentTemplate().formatted(parameter.getName()));
                } else if (initArgument.identifier != null) {
                    appendWithIndentation(builder, numberOfSpaces, fileTemplate.getArgumentTemplate().formatted(initArgument.identifier));
                }
            }

            if (initArgumentsForParameter.size() > 1) {
                builder.append("\n");
            }
        }
        builder.append("\n").append(" ".repeat(12));
        return builder.toString();
    }

    private static OrderedHashMap<PyParameter, List<IdentifierItem>> groupIdentifiersByParameter(Collection<IdentifierItem> identifiers) {
        return identifiers.stream()
                .collect(Collectors.groupingBy(IdentifierItem::getParameter, OrderedHashMap::new, Collectors.toList()));
    }

    private static void appendWithIndentation(StringBuilder builder, int numberOfSpaces, String content) {
        builder.append("\n").append(" ".repeat(numberOfSpaces)).append(content);
    }
}
