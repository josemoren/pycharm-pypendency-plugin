package org.fever.filecreator.templates;

import com.intellij.openapi.fileTypes.FileType;

public interface DIFileTemplate {
    String getFileExtension();
    FileType getFileType();
    int getArgumentIndentationSpaces();
    String getBaseTemplate();
    String getArgumentStatementBeginning();
    String getMultipleArgumentsTemplateBeginning();
    String getMultipleArgumentsTemplateEnd();
    String getMissingArgumentTemplate();
    String getArgumentTemplate();
    String getArgumentStatementEnd();
}
