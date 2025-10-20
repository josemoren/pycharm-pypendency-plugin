package org.fever.filecreator.templates;

import com.intellij.openapi.fileTypes.FileType;
import org.fever.filecreator.DIFileType;

public class YamlDIFileTemplate implements DIFileTemplate {
    private static final int ARGUMENT_INDENTATION_SPACES = 8;
    private static final String BASE_TEMPLATE = """
        {identifier}:
            fqn: {fqn}{arguments}
        """;
    private static final String ARGUMENT_STATEMENT_BEGINNING = "\n    args:";
    private static final String MULTIPLE_ARGUMENTS_TEMPLATE_BEGINNING = "# TODO: 👇 Multiple arguments found for %s, leave only one:";
    private static final String MULTIPLE_ARGUMENTS_TEMPLATE_END = "# TODO: 👆";
    private static final String MISSING_ARGUMENT_TEMPLATE = "- \"@\"  # TODO: missing argument for \"%s\" of type \"%s\"";
    private static final String ARGUMENT_TEMPLATE = "- \"@%s\"";
    private static final String ARGUMENT_STATEMENT_END = "";

    @Override
    public String getFileExtension() {
        return DIFileType.YAML.getFileExtension();
    }

    @Override
    public FileType getFileType() {
        return DIFileType.YAML.getFileType();
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
