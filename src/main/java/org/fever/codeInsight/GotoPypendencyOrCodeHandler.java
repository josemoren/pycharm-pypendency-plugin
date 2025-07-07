package org.fever.codeInsight;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.jetbrains.python.psi.PyClass;
import org.fever.filecreator.DIFileType;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.fever.utils.DIFileOpener;
import org.fever.utils.PyClassUnderCaretFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;


public class GotoPypendencyOrCodeHandler extends GotoTargetHandler {
    public static final String FEATURE_KEY = "navigation.goto.pypendencyOrCode";
    public static final String ACTIONS_TITLE = "Pypendency actions";
    public static final String CREATE_NEW_YAML_DEFINITION = "Create new yaml definition...";
    public static final String CREATE_NEW_PYTHON_DEFINITION = "Create new python definition...";
    public static final String NOT_FOUND = "Not found";
    public static final String DEPENDENCY_INJECTION_FOLDER = "/_dependency_injection/";
    AnActionEvent anActionEvent;

    public GotoPypendencyOrCodeHandler(AnActionEvent anActionEvent) {
        super();
        this.anActionEvent = anActionEvent;
    }

    @Override
    protected String getFeatureUsedKey() {
        return FEATURE_KEY;
    }

    @Override
    protected @NotNull String getChooserTitle(@NotNull PsiElement sourceElement, @Nullable String name, int length, boolean finished) {
        return ACTIONS_TITLE;
    }

    @Override
    protected @Nullable GotoData getSourceAndTargetElements(Editor editor, PsiFile file) {
        PyClass pyClassUnderCaret = PyClassUnderCaretFinder.find(editor, file);
        String fqn = pyClassUnderCaret.getQualifiedName();
        Collection<PsiFile> diFiles = DependencyInjectionFileResolverByIdentifier.findAll(file.getManager(), fqn);

        if (!diFiles.isEmpty()) {
            return getGotoDataForExistingPypendency(pyClassUnderCaret, diFiles);
        }

        return getGotoDataForNewPypendency(editor, file, pyClassUnderCaret);
    }

    @NotNull
    private GotoTargetHandler.GotoData getGotoDataForExistingPypendency(PsiElement pyClassUnderCaret, Collection<PsiFile> diFiles) {
        PsiElement[] targets = diFiles.toArray(new PsiElement[0]);
        return new GotoData(pyClassUnderCaret, targets, new SmartList<>());
    }

    @NotNull
    private GotoTargetHandler.GotoData getGotoDataForNewPypendency(Editor editor, PsiFile file, PsiElement pyClassUnderCaret) {
        List<AdditionalAction> actions = new SmartList<>();

        actions.add(new AdditionalAction() {
            @NotNull
            @Override
            public String getText() {
                return CREATE_NEW_YAML_DEFINITION;
            }

            @Override
            public Icon getIcon() {
                return AllIcons.Actions.IntentionBulb;
            }

            @Override
            public void execute() {
                DIFileOpener.open(editor, file, DIFileType.YAML);
            }
        });

        actions.add(new AdditionalAction() {
            @NotNull
            @Override
            public String getText() {
                return CREATE_NEW_PYTHON_DEFINITION;
            }

            @Override
            public Icon getIcon() {
                return AllIcons.Actions.IntentionBulb;
            }

            @Override
            public void execute() {
                DIFileOpener.open(editor, file, DIFileType.PYTHON);
            }
        });

        return new GotoData(pyClassUnderCaret, PsiElement.EMPTY_ARRAY, actions);
    }

    @Override
    protected @NotNull String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return NOT_FOUND;
    }
}
