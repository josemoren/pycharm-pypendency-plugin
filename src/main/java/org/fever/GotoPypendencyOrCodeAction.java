package org.fever;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.codeInsight.generation.actions.PresentableActionHandlerBasedAction;
import com.intellij.lang.CodeInsightActions;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.fever.GotoPypendencyOrCodeHandler;
import org.jetbrains.annotations.NotNull;

public class GotoPypendencyOrCodeAction extends PresentableActionHandlerBasedAction {
    @Override
    @NotNull
    protected CodeInsightActionHandler getHandler(){
        return new GotoPypendencyOrCodeHandler();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible(true);
        presentation.setText("Pypendency");
        presentation.setDescription("Open pypendency definition...");
        super.update(e);
    }

    @NotNull
    @Override
    protected LanguageExtension<CodeInsightActionHandler> getLanguageExtension() {
        return CodeInsightActions.GOTO_SUPER;
    }
}