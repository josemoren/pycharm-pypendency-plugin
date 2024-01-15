package org.fever.utils;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

public class SourceCodeFileResolver {
    public static @Nullable PsiFile fromFqn(String fqn, PsiManager psiManager) {
        String fqnWithSlashes = fqn.replace(".", "/");
        String relativeFilePath = fqnWithSlashes.substring(0, fqnWithSlashes.lastIndexOf("/"));
        String absoluteBasePath = psiManager.getProject().getBasePath();

        String[] possibleFilePaths = {
                absoluteBasePath + "/src/" + relativeFilePath + ".py",
                absoluteBasePath + "/src/" + relativeFilePath + "/" + getClassNameInSnakeCase(fqn) + ".py",
        };

        for (String filePath : possibleFilePaths) {
            PsiFile file = getFileFromAbsolutePath(filePath, psiManager);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    public static String getClassNameInSnakeCase(String fqn) {
        String[] parts = fqn.split("\\.");
        String className = parts[parts.length - 1];
        return CaseFormatter.camelCaseToSnakeCase(className);
    }

    public static @Nullable PsiFile getFileFromAbsolutePath(String absolutePath, PsiManager psiManager) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(absolutePath);
        if (file != null) {
            return psiManager.findFile(file);
        }
        return null;
    }
}
