package org.fever.utils;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.fever.codeInsight.GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER;

public class DIPathRetriever {
    public static @Nullable VirtualFile retrieve(@NotNull PsiFile file) {
        PsiDirectory directory = file.getParent();
        String directoryPath;

        while (directory != null) {
            directoryPath = directory.getVirtualFile().getCanonicalPath();

            if (FileUtil.exists(directoryPath + DEPENDENCY_INJECTION_FOLDER)) {
                return LocalFileSystem.getInstance().findFileByPath(directoryPath + DEPENDENCY_INJECTION_FOLDER);
            }

            directory = directory.getParent();
        }

        return null;
    }
}
