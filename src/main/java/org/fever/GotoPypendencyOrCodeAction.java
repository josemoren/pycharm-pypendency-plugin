package org.fever;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.PresentableActionHandlerBasedAction;
import com.intellij.lang.CodeInsightActions;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class GotoPypendencyOrCodeAction extends PresentableActionHandlerBasedAction {
    public static final String PRESENTATION_TEXT = "Pypendency";
    public static final String PRESENTATION_DESCRIPTION = "Open pypendency definition...";

    AnActionEvent anActionEvent;

    @Override
    @NotNull
    protected CodeInsightActionHandler getHandler(){
        return new GotoPypendencyOrCodeHandler(this.anActionEvent);
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