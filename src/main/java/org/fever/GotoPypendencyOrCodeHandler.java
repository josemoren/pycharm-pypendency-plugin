package org.fever;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;


public class GotoPypendencyOrCodeHandler  extends GotoTargetHandler {
    AnActionEvent e;
    public GotoPypendencyOrCodeHandler(AnActionEvent e) {
        super();
        this.e = e;
    }

    @Override
    protected String getFeatureUsedKey() {
        return "navigation.goto.pypendencyOrCode";
    }

    @Override
    protected @Nullable GotoData getSourceAndTargetElements(Editor editor, PsiFile file) {
        List<AdditionalAction> actions = new SmartList<>();
        GotoPypendencyOrCodeHandler self = this;

        actions.add(new AdditionalAction() {
            @NotNull
            @Override
            public String getText() {
                String text = null;
                return ObjectUtils.notNull(text, "Create new pypendency yaml...");
            }

            @Override
            public Icon getIcon() {
                return ObjectUtils.notNull(null, AllIcons.Actions.IntentionBulb);
            }

            @Override
            public void execute() {
                self.createPypendencyYaml(editor, file);
            }
        });

        return new GotoData(PsiUtilCore.getElementAtOffset(file, editor.getCaretModel().getOffset()), PsiElement.EMPTY_ARRAY, actions);
    }

    private void createPypendencyYaml(Editor editor, PsiFile file) {
        System.out.println("I want to create a new YAML!");
        AnAction action = ActionManager.getInstance().getAction(IdeActions.ACTION_COPY_REFERENCE);
        action.actionPerformed(this.e);
        Transferable [] transferables = CopyPasteManager.getInstance().getAllContents();

        String fqn = null;
        try {
            fqn = transferables[transferables.length-1].getTransferData(DataFlavor.stringFlavor).toString();
        } catch (UnsupportedFlavorException unsupportedFlavorException) {
            unsupportedFlavorException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        PsiFile psiFile = PsiFileFactory.getInstance(file.getProject()).createFileFromText("hola.yaml", YAMLFileType.YML, fqn + ":\n    fqn: " + fqn);
        PsiFile new_file = (PsiFile) file.getContainingDirectory().add(psiFile);
        FileEditorManager.getInstance(editor.getProject()).openFile(new_file.getVirtualFile(), true);
        System.out.println("...");
    }

    @Override
    protected @NotNull String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return null;
    }

}
