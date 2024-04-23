package org.fever.filecreator.templates;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.yaml.YAMLFileType;

public class YamlDIFileTemplate implements DIFileTemplate {
    private static final String FILE_EXTENSION = ".yaml";
    private static final FileType FILE_TYPE = YAMLFileType.YML;
    private static final int ARGUMENT_INDENTATION_SPACES = 8;
    private static final String BASE_TEMPLATE = """
            {identifier}:
                fqn: {fqn}{arguments}
            """;
    private static final String ARGUMENT_STATEMENT_BEGINNING = "\n    args:";
    private static final String MULTIPLE_ARGUMENTS_TEMPLATE = "# TODO: multiple arguments found for %s, leave only one:";
    private static final String MISSING_ARGUMENT_TEMPLATE = "# TODO: missing argument for \"%s\"";
    private static final String ARGUMENT_TEMPLATE = "- \"@%s\"";
    private static final String ARGUMENT_STATEMENT_END = null;

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
    public String getMultipleArgumentsTemplate() {
        return MULTIPLE_ARGUMENTS_TEMPLATE;
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
