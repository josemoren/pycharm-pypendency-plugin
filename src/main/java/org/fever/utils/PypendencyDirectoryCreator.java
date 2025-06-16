package org.fever.utils;

import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

public class PypendencyDirectoryCreator {
    public static PsiDirectory forFile(PsiFile file) {
        VirtualFile diPath = DIPathRetriever.retrieve(file);
        assert diPath != null;

        PsiDirectory fileParent = file.getParent();
        assert fileParent != null;

        VirtualFile parentVirtualFile = fileParent.getVirtualFile();
        String relativePath = VfsUtilCore.getRelativePath(parentVirtualFile, diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;

        Project fileProject = file.getProject();
        return WriteAction.compute(
                () -> DirectoryUtil.mkdirs(PsiManager.getInstance(fileProject), diNewPath)
        );
    }
}
