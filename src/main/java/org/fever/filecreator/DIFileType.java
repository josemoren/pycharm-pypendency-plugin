package org.fever.filecreator;

import org.jetbrains.annotations.NotNull;

public enum DIFileType {
    PYTHON("py", "Python"),
    YAML("yaml", "YAML");

    private final String extension;
    private final String fileExtension;
    private final String name;

    /**
     * Constructs a new {@link DIFileType} with the specified extension and name.
     *
     * @param extension the file extension without the dot.
     * @param name      the name of the file type.
     */
    DIFileType(@NotNull String extension, @NotNull String name) {
        this.extension = extension;
        this.fileExtension = "." + extension;
        this.name = name;
    }

    /**
     * Returns the file extension without the dot.
     *
     * @return the file extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the file extension with the dot.
     *
     * @return the file extension with a leading dot.
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Returns the name of the file type.
     *
     * @return the name of the file type.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link DIFileType} corresponding to the given file extension.
     *
     * @param extension the file extension to match.
     * @return the {@link DIFileType} corresponding to the given extension.
     * @throws IllegalArgumentException if no matching {@link DIFileType} is found.
     */
    public static DIFileType fromExtension(@NotNull String extension) {
        for (DIFileType type : values()) {
            if (type.extension.equalsIgnoreCase(extension)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown file extension: " + extension);
    }
}
