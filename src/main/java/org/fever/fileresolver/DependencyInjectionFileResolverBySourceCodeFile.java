package org.fever.fileresolver;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import org.fever.utils.PyClassUnderCaretFinder;

public class DependencyInjectionFileResolverBySourceCodeFile {
    public static PsiFile resolve(Editor editor, Project project, PsiFile file) {
        PyClass closestPyClass = PyClassUnderCaretFinder.find(editor, file);
        PsiFile closestPyClassPypendencyDefinitionFile = DependencyInjectionFileResolverByClassName.resolve(project, closestPyClass.getName());
        if (closestPyClassPypendencyDefinitionFile != null) {
            return closestPyClassPypendencyDefinitionFile;
        }

        for (PyClass pyClass : ((PyFile) file).getTopLevelClasses()) {
            PsiFile pypendencyDefinitionFile = DependencyInjectionFileResolverByClassName.resolve(project, pyClass.getName());
            if (pypendencyDefinitionFile != null) {
                return pypendencyDefinitionFile;
            }
        }
        return null;
    }
}
