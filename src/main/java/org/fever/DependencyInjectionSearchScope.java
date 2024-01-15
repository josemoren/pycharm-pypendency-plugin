package org.fever;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class DependencyInjectionSearchScope extends GlobalSearchScope {

    public DependencyInjectionSearchScope(@NotNull Project project) {
        super(project);
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        String filePath = file.getPath();
        return isPythonFile(file) && fileIsInDependencyInjectionFolder(filePath) && fileIsNotInExternalLibraries(filePath);
    }

    private static boolean fileIsInDependencyInjectionFolder(String filePath) {
        return filePath.contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER);
    }

    private static boolean isPythonFile(@NotNull VirtualFile file) {
        return file.getName().endsWith(".py");
    }

    private static boolean fileIsNotInExternalLibraries(String filePath) {
        return !filePath.contains("/remote_sources/");
    }

    @Override
    public int compare(@NotNull VirtualFile file1, @NotNull VirtualFile file2) {
        return 0;
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull com.intellij.openapi.module.Module aModule) {
        return false;
    }

    @Override
    public boolean isSearchInLibraries() {
        return false;
    }
}