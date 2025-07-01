package org.fever.utils;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import org.fever.ResolutionCache;
import org.fever.filecreator.DIFileCreator;
import org.fever.filecreator.DIFileType;
import org.jetbrains.annotations.Nullable;

public class DIFileOpener {
    public static void open(Editor editor, PsiFile file, DIFileType type) {
        PyClass targetPyClass = PyClassUnderCaretFinder.find(editor, file);
        if (targetPyClass == null) {
            return;
        }

        String fqn = targetPyClass.getQualifiedName();
        PsiFile newFile = getPsiFile(file, type, targetPyClass, fqn);

        assert newFile != null;
        String createdFilePath = newFile.getVirtualFile().getCanonicalPath();
        ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
        Project fileProject = file.getProject();

        resolutionCache.setCachedResolution(fileProject.getName(), fqn, createdFilePath);
        FileEditorManager.getInstance(fileProject).openFile(newFile.getVirtualFile(), true);
    }

    private static @Nullable PsiFile getPsiFile(PsiFile file, DIFileType type, PyClass targetPyClass, String fqn) {
        PsiDirectory directory = PypendencyDirectoryCreator.forFile(file);
        PsiFile dependencyInjectionFile = DIFileCreator.create(targetPyClass, fqn, type);
        PsiFile newFile;

        try {
            newFile = WriteAction.compute(() -> (PsiFile) directory.add(dependencyInjectionFile));
        } catch (Exception e) {
            newFile = directory.findFile(dependencyInjectionFile.getName());
        }

        return newFile;
    }
}
