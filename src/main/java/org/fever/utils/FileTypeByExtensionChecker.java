package org.fever.utils;

import static org.fever.filecreator.DIFileType.PYTHON;
import static org.fever.filecreator.DIFileType.YAML;

public class FileTypeByExtensionChecker {
    public static boolean isYamlFile(String extension) {
        return extension.equals(YAML.getExtension()) || extension.equals("yml"); // Todo: is yml extension needed?
    }

    public static boolean isPythonFile(String extension) {
        return extension.equals(PYTHON.getExtension());
    }
}
