package org.fever;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.QualifiedNameProviderUtil;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;

import javax.swing.*;
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
        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement elementUnderCaret = file.findElementAt(caretOffset);

        if (elementUnderCaret == null) {
            return null;
        }

        PsiFile pypendencyDefinition = this.getPypendencyDefinition(file);

        if (pypendencyDefinition != null) {
            return getGotoDataForExistingPypendency(elementUnderCaret, pypendencyDefinition);
        }

        return getGotoDataForNewPypendency(editor, file, elementUnderCaret, this);
    }

    private @Nullable String getCurrentFQN(Editor editor, PsiFile file) {
        int caretOffset = editor.getCaretModel().getOffset();
        PsiElement elementUnderCaret = file.findElementAt(caretOffset);

        PsiElement elementParent = elementUnderCaret.getParent();
        if (elementParent == null) {
            return null;
        }

        return QualifiedNameProviderUtil.getQualifiedName(elementParent);
    }

    @NotNull
    private GotoTargetHandler.@Nullable GotoData getGotoDataForExistingPypendency(PsiElement elementUnderCaret, PsiFile pypendencyDefinition) {
        PsiElement[] targets = new PsiElement[]{pypendencyDefinition};
        return new GotoData(
                elementUnderCaret,
                targets,
                new SmartList<>()
        );
    }

    @NotNull
    private GotoTargetHandler.@Nullable GotoData getGotoDataForNewPypendency(Editor editor, PsiFile file, PsiElement elementUnderCaret, GotoPypendencyOrCodeHandler self) {
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
                self.createPypendencyYaml(editor, file);
            }
        });

        actions.add(new AdditionalAction() {
            @NotNull
            @Override
            public String getText() {
                return  CREATE_NEW_PYTHON_DEFINITION;
            }

            @Override
            public Icon getIcon() {
                return AllIcons.Actions.IntentionBulb;
            }

            @Override
            public void execute() {
                self.createPypendencyPython(editor, file);
            }
        });

        return new GotoData(elementUnderCaret, PsiElement.EMPTY_ARRAY, actions);
    }

    private @Nullable PsiFile getPypendencyDefinition(PsiFile file) {
        VirtualFile diPath = this.getDIPath(file);
        if (diPath == null) {
            return null;
        }

        PsiDirectory fileParent = file.getParent();
        if (fileParent == null) {
            return null;
        }

        String relativePath = VfsUtilCore.getRelativePath(fileParent.getVirtualFile(), diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;
        String yamlName = file.getName().replace(".py", ".yaml");

        String diFile = diNewPath + "/" + yamlName;
        if (FileUtil.exists(diFile)) {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(diFile);
            assert fileByPath != null;
            return PsiManager.getInstance(file.getProject()).findFile(
                    fileByPath
            );
        }

        diFile = diNewPath + "/" + file.getName();
        if (FileUtil.exists(diFile)) {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(diFile);
            assert fileByPath != null;
            return PsiManager.getInstance(file.getProject()).findFile(
                    fileByPath
            );
        }

        return null;
    }

    private void createPypendencyYaml(Editor editor, PsiFile file) {
        VirtualFile diPath = this.getDIPath(file);
        if (diPath == null) {
            return;
        }

        Project editorProject = editor.getProject();
        if (editorProject == null) {
            return;
        }

        PsiDirectory fileParent = file.getParent();
        if (fileParent == null) {
            return;
        }

        VirtualFile parentVirtualFile = fileParent.getVirtualFile();
        String relativePath = VfsUtilCore.getRelativePath(parentVirtualFile, diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;

        PsiDirectory directory = WriteAction.compute(
                () -> DirectoryUtil.mkdirs(PsiManager.getInstance(editorProject), diNewPath)
        );

        String fqn = this.getCurrentFQN(editor, file);
        if (fqn == null) {
            return;
        }
        PsiFile psiFile = PsiFileFactory.getInstance(file.getProject()).createFileFromText(
                file.getName().replace(".py", ".yaml"), YAMLFileType.YML, fqn + ":\n    fqn: " + fqn);

        PsiFile new_file = WriteAction.compute(
                () -> (PsiFile) directory.add(psiFile)
        );
        FileEditorManager.getInstance(editorProject).openFile(new_file.getVirtualFile(), true);
    }

    private void createPypendencyPython(Editor editor, PsiFile file) {
        VirtualFile diPath = this.getDIPath(file);

        if (diPath == null) {
            return;
        }

        Project editorProject = editor.getProject();
        if (editorProject == null) {
            return;
        }

        PsiDirectory fileParent = file.getParent();
        if (fileParent == null) {
            return;
        }

        String relativePath = VfsUtilCore.getRelativePath(fileParent.getVirtualFile(), diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;
        PsiDirectory directory = WriteAction.compute(
                () -> DirectoryUtil.mkdirs(PsiManager.getInstance(editorProject), diNewPath)
        );

        String fqn = this.getCurrentFQN(editor, file);
        if (fqn == null) {
            return;
        }
        String text = "from pypendency.argument import Argument\n" +
                "from pypendency.builder import ContainerBuilder\n" +
                "from pypendency.definition import Definition\n" +
                "\n" +
                "from django.conf import settings\n" +
                "\n" +
                "\n" +
                "def load(container_builder: ContainerBuilder):\n" +
                "    container_builder.set_definition(\n" +
                "        Definition(\n" +
                "            \"" + fqn + "\",\n" +
                "            \"" + fqn + "\",\n" +
                "            [\n" +
                "                Argument.no_kw_argument(),\n" +
                "            ],\n" +
                "        )\n" +
                "    )\n" +
                "";
        PsiFile psiFile = PsiFileFactory.getInstance(file.getProject()).createFileFromText(
                file.getName(), YAMLFileType.YML, text);

        PsiFile new_file = WriteAction.compute(
                () -> (PsiFile) directory.add(psiFile)
        );
        FileEditorManager.getInstance(editorProject).openFile(new_file.getVirtualFile(), true);
    }

    private @Nullable VirtualFile getDIPath(@NotNull PsiFile file) {
        PsiDirectory directory = file.getParent();

        while (directory != null) {
            String directoryPath = directory.getVirtualFile().getCanonicalPath();

            if (FileUtil.exists(directoryPath + DEPENDENCY_INJECTION_FOLDER)) {
                return LocalFileSystem.getInstance().findFileByPath(directoryPath + DEPENDENCY_INJECTION_FOLDER);
            }

            directory = directory.getParent();
        }

        return null;
    }

    @Override
    protected @NotNull String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return NOT_FOUND;
    }
}
