package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyParameter;
import groovyjarjarantlr4.v4.misc.OrderedHashMap;
import org.fever.filecreator.templates.DIFileTemplate;
import org.fever.filecreator.templates.PythonDIFileTemplate;
import org.fever.filecreator.templates.YamlDIFileTemplate;
import org.fever.utils.ClassArgumentParser;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DIFileCreator {

    public static PsiFile create(PyFile sourceCodeFile, String identifier, DIFileType type) {
        DIFileTemplate fileTemplate = getDIFileTemplate(type);
        String fileContent = fileTemplate.getBaseTemplate()
                .replace("{identifier}", identifier)
                .replace("{fqn}", identifier)
                .replace("{arguments}", getArguments(sourceCodeFile, fileTemplate));

        return PsiFileFactory.getInstance(sourceCodeFile.getProject()).createFileFromText(
                sourceCodeFile.getName().replace(".py", fileTemplate.getFileExtension()),
                fileTemplate.getFileType(),
                fileContent
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

            if (numberOfImplementationsForParameter(initArgumentsForParameter) > 1) {
                appendWithIndentation(builder, numberOfSpaces, fileTemplate.getMultipleArgumentsTemplateBeginning().formatted(parameter.getName()));
            }
            for (IdentifierItem initArgument : initArgumentsForParameter) {
                if (initArgument.isNotAClass() || (initArgument.hasNoImplementations() && initArgumentsForParameter.size() == 1)) {
                    String parameterName = parameter.getName();
                    @Nullable String parameterClass = ClassArgumentParser.parse(parameter.getText());
                    if (parameterClass == null) parameterClass = "Unknown";
                    appendWithIndentation(builder, numberOfSpaces, fileTemplate.getMissingArgumentTemplate().formatted(parameterName, parameterClass));
                } else if (initArgument.identifier != null) {
                    appendWithIndentation(builder, numberOfSpaces, fileTemplate.getArgumentTemplate().formatted(initArgument.identifier));
                }
            }

            if (numberOfImplementationsForParameter(initArgumentsForParameter) > 1) {
                appendWithIndentation(builder, numberOfSpaces, fileTemplate.getMultipleArgumentsTemplateEnd());
            }
        }
        builder.append(fileTemplate.getArgumentStatementEnd());
        return builder.toString();
    }

    private static long numberOfImplementationsForParameter(List<IdentifierItem> initArgumentsForParameter) {
        return initArgumentsForParameter.stream().filter(x -> x.identifier != null).count();
    }

    private static OrderedHashMap<PyParameter, List<IdentifierItem>> groupIdentifiersByParameter(Collection<IdentifierItem> identifiers) {
        return identifiers.stream()
                .collect(Collectors.groupingBy(IdentifierItem::getParameter, OrderedHashMap::new, Collectors.toList()));
    }

    private static void appendWithIndentation(StringBuilder builder, int numberOfSpaces, String content) {
        builder.append("\n").append(" ".repeat(numberOfSpaces)).append(content);
    }
}
