package org.fever.filecreator;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;

import javax.swing.*;

public enum DIFileType {
    PYTHON("py", PythonFileType.INSTANCE),
    YAML("yaml", YAMLFileType.YML);

    private final String extension;
    private final String fileExtension;
    private final LanguageFileType fileType;

    DIFileType(@NotNull String extension, LanguageFileType fileType) {
        this.extension = extension;
        this.fileExtension = "." + extension;
        this.fileType = fileType;
    }

    public String getExtension() {
        return extension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getName() {
        return fileType.getName();
    }

    public Icon getIcon() {
        return fileType.getIcon();
    }

    public LanguageFileType getFileType() {
        return fileType;
    }

    public static DIFileType fromExtension(@NotNull String extension) {
        for (DIFileType type : values()) {
            if (type.extension.equalsIgnoreCase(extension)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown file extension: " + extension);
    }
}
