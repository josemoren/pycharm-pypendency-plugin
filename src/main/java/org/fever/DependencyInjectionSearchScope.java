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
        return file.getPath().contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER) && file.getName().endsWith(".py");
    }

    @Override
    public int compare(@NotNull VirtualFile file1, @NotNull VirtualFile file2) {
        return 0;
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull com.intellij.openapi.module.Module aModule) {
        return true;
    }

    @Override
    public boolean isSearchInLibraries() {
        return false;
    }
}