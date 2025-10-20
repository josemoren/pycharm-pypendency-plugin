package org.fever.filecreator.templates;

import com.intellij.openapi.fileTypes.FileType;
import com.jetbrains.python.PythonFileType;

public class PythonDIFileTemplate implements DIFileTemplate {
    private static final String FILE_EXTENSION = ".py";
    private static final FileType FILE_TYPE = PythonFileType.INSTANCE;
    private static final int ARGUMENT_INDENTATION_SPACES = 16;
    private static final String BASE_TEMPLATE = """
            from pypendency.argument import Argument
            from pypendency.builder import ContainerBuilder
            from pypendency.definition import Definition


            def load(container_builder: ContainerBuilder) -> None:
                container_builder.set_definition(
                    Definition(
                        "{fqn}",
                        "{fqn}",
                        [{arguments}],
                    )
                )
            """;
    private static final String ARGUMENT_STATEMENT_BEGINNING = "";
    private static final String MULTIPLE_ARGUMENTS_TEMPLATE_BEGINNING = "# TODO: 👇 Multiple arguments found for %s, leave only one:";
    private static final String MULTIPLE_ARGUMENTS_TEMPLATE_END = "# TODO: 👆";
    private static final String MISSING_ARGUMENT_TEMPLATE = "Argument.no_kw_argument(\"@\"),  # TODO: missing argument for \"%s\" of type \"%s\"";
    private static final String ARGUMENT_TEMPLATE = "Argument.no_kw_argument(\"@%s\"),";
    private static final String ARGUMENT_STATEMENT_END = "\n            ";

    @Override
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public FileType getFileType() {
        return FILE_TYPE;
    }

    @Override
    public int getArgumentIndentationSpaces() {
        return ARGUMENT_INDENTATION_SPACES;
    }

    @Override
    public String getBaseTemplate() {
        return BASE_TEMPLATE;
    }

    @Override
    public String getArgumentStatementBeginning() {
        return ARGUMENT_STATEMENT_BEGINNING;
    }

    @Override
    public String getMultipleArgumentsTemplateBeginning() {
        return MULTIPLE_ARGUMENTS_TEMPLATE_BEGINNING;
    }

    @Override
    public String getMultipleArgumentsTemplateEnd() {
        return MULTIPLE_ARGUMENTS_TEMPLATE_END;
    }

    @Override
    public String getMissingArgumentTemplate() {
        return MISSING_ARGUMENT_TEMPLATE;
    }

    @Override
    public String getArgumentTemplate() {
        return ARGUMENT_TEMPLATE;
    }

    @Override
    public String getArgumentStatementEnd() {
        return ARGUMENT_STATEMENT_END;
    }
}
