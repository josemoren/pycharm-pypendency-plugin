package org.fever;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.PresentableActionHandlerBasedAction;
import com.intellij.lang.CodeInsightActions;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import static org.fever.GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER;

public class GotoPypendencyOrCodeAction extends PresentableActionHandlerBasedAction {
    public static final String PRESENTATION_TEXT = "Pypendency: Go to/create DI file";
    public static final String PRESENTATION_DESCRIPTION = "Open or create pypendency definition...";

    AnActionEvent anActionEvent;

    @Override
    @NotNull
    protected CodeInsightActionHandler getHandler(){
        PsiFile file = this.anActionEvent.getData(DataKey.create("psi.File"));
        assert file != null;
        if (isDependencyInjectionFile(file)) {
            return new GotoCodeHandler(this.anActionEvent);
        } else {
            return new GotoPypendencyOrCodeHandler(this.anActionEvent);
        }
    }

    private Boolean isDependencyInjectionFile(PsiFile file) {
        String filePath = file.getVirtualFile().getCanonicalPath();
        assert filePath != null;
        return filePath.contains(DEPENDENCY_INJECTION_FOLDER);
    }

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        final Presentation presentation = anActionEvent.getPresentation();
        presentation.setEnabledAndVisible(true);
        presentation.setText(PRESENTATION_TEXT);
        presentation.setDescription(PRESENTATION_DESCRIPTION);
        super.update(anActionEvent);
        this.anActionEvent = anActionEvent;
    }

    @NotNull
    @Override
    protected LanguageExtension<CodeInsightActionHandler> getLanguageExtension() {
        return CodeInsightActions.GOTO_SUPER;
    }
}