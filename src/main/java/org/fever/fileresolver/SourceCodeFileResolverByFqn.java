package org.fever.fileresolver;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.fever.utils.CaseFormatter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class SourceCodeFileResolverByFqn {
    public static @Nullable PsiFile resolve(String fqn, PsiManager psiManager) {
        Collection<String> sourceRootAbsolutePaths = getFoldersMarkedAsSourceRoots(psiManager.getProject());
        Collection<String> possibleFilePaths = getPossibleFilePaths(sourceRootAbsolutePaths, fqn);

        for (String filePath : possibleFilePaths) {
            PsiFile file = getFileFromAbsolutePath(filePath, psiManager);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private static Collection<String> getFoldersMarkedAsSourceRoots(Project project) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module module = moduleManager.getModules()[0];

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        ContentEntry contentEntry = moduleRootManager.getContentEntries()[0];
        SourceFolder[] sourceFolders = contentEntry.getSourceFolders();

        return Arrays.stream(sourceFolders)
                .map(folder -> folder.getJpsElement().getPath().toAbsolutePath().toString())
                .toList();
    }

    private static Collection<String> getPossibleFilePaths(Collection<String> sourceCodeRootAbsolutePaths, String fqn) {
        String fqnWithSlashes = fqn.replace(".", "/");
        String relativeFilePath = fqnWithSlashes.substring(0, fqnWithSlashes.lastIndexOf("/"));
        Collection<String> possibleFilePaths = new java.util.ArrayList<>();
        for (String sourceCodeRootAbsolutePath : sourceCodeRootAbsolutePaths) {
            String folderName = sourceCodeRootAbsolutePath.substring(sourceCodeRootAbsolutePath.lastIndexOf("/") + 1);
            String className = fqn.substring(fqn.lastIndexOf(".") + 1);
            if (relativeFilePath.startsWith(folderName + "/")) {
                relativeFilePath = relativeFilePath.substring(folderName.length() + 1);
            }
            possibleFilePaths.add(sourceCodeRootAbsolutePath + "/" + relativeFilePath + ".py");
            possibleFilePaths.add(sourceCodeRootAbsolutePath + "/" + relativeFilePath + "/" + CaseFormatter.camelCaseToSnakeCase(className) + ".py");
        }
        return possibleFilePaths;
    }


    public static @Nullable PsiFile getFileFromAbsolutePath(String absolutePath, PsiManager psiManager) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(absolutePath);
        if (file != null) {
            return psiManager.findFile(file);
        }
        return null;
    }
}
