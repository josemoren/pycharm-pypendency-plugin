package org.fever;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.*;
import com.intellij.util.SmartList;
import org.fever.utils.SourceCodeFileResolver;
import org.fever.utils.FqnExtractor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GotoCodeHandler extends GotoTargetHandler {
    public static final String FEATURE_KEY = "navigation.goto.codeFromDIFile";
    public static final String NOT_FOUND = "Not found";

    AnActionEvent anActionEvent;

    public GotoCodeHandler(AnActionEvent anActionEvent) {
        super();
        this.anActionEvent = anActionEvent;
    }

    @Override
    protected String getFeatureUsedKey() {
        return FEATURE_KEY;
    }

    @Override
    @Nullable
    protected GotoData getSourceAndTargetElements(Editor editor, PsiFile dependencyInjectionFile) {
        String dependencyInjectionFilePath = dependencyInjectionFile.getVirtualFile().getCanonicalPath();
        assert dependencyInjectionFilePath != null;

        if (!dependencyInjectionFilePath.contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER)) {
            return null;
        }

        String fqn = parseMainFqn(dependencyInjectionFile);
        if (fqn == null) {
            return null;
        }

        PsiFile sourceCodeFile = SourceCodeFileResolver.fromFqn(fqn, dependencyInjectionFile.getManager());
        if (sourceCodeFile == null) {
            return null;
        }

        PsiElement[] targets = new PsiElement[]{sourceCodeFile};
        return new GotoData(
                dependencyInjectionFile,
                targets,
                new SmartList<>());
    }

    private @Nullable String parseMainFqn(PsiFile dependencyInjectionFile) {
        String extension = dependencyInjectionFile.getVirtualFile().getExtension();
        if (extension == null) {
            return null;
        }

        String fileContent = dependencyInjectionFile.getText();
        if (isYamlFile(extension)) {
            return FqnExtractor.extractFqnFromYaml(fileContent);
        }
        if (isPythonFile(extension)) {
            return FqnExtractor.extractFqnFromPython(fileContent);
        }
        return null;
    }

    private Boolean isYamlFile(String extension) {
        return extension.equals("yaml") || extension.equals("yml");
    }

    private Boolean isPythonFile(String extension) {
        return extension.equals("py");
    }

    @Override
    protected @NotNull @NlsContexts.HintText String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return NOT_FOUND;
    }
}
