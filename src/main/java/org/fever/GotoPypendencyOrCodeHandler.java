package org.fever;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GotoPypendencyOrCodeHandler  extends GotoTargetHandler {
    @Override
    protected String getFeatureUsedKey() {
        return null;
    }

    @Override
    protected @Nullable GotoData getSourceAndTargetElements(Editor editor, PsiFile file) {
        return null;
    }

    @Override
    protected @NotNull String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return null;
    }
}
