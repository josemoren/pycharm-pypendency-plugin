package org.fever;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class GotoInjectedImplementationAction extends BaseCodeInsightAction {
    public static final String PRESENTATION_TEXT = "Pypendency: Go to the injected implementation";
    public static final String PRESENTATION_DESCRIPTION = "Go to the injected implementation for the selected class";

    AnActionEvent anActionEvent;

    @Override
    @NotNull
    protected CodeInsightActionHandler getHandler(){
        return new GotoInjectedImplementationHandler(this.anActionEvent);
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
}
