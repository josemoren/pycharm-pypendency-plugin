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
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ObjectUtils;
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

    AnActionEvent anActionEvent;
    String currentFQN = null;

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
        PsiElement element = file.findElementAt(caretOffset);
        this.currentFQN = QualifiedNameProviderUtil.getQualifiedName(element.getParent());
        if (this.currentFQN == null) return null;

        List<AdditionalAction> actions = new SmartList<>();
        GotoPypendencyOrCodeHandler self = this;

        PsiFile pypendencyDefinition = this.getPypendencyDefinition(file);

        if (pypendencyDefinition != null) {
            PsiElement[] targets = new PsiElement[]{pypendencyDefinition};
            return new GotoData(
                    PsiUtilCore.getElementAtOffset(file, editor.getCaretModel().getOffset()),
                    targets,
                    actions
            );
        }

        actions.add(new AdditionalAction() {
            @NotNull
            @Override
            public String getText() {
                String text = null;
                return ObjectUtils.notNull(text, CREATE_NEW_YAML_DEFINITION);
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

        actions.add(new AdditionalAction() {
            @NotNull
            @Override
            public String getText() {
                String text = null;
                return ObjectUtils.notNull(text, CREATE_NEW_PYTHON_DEFINITION);
            }

            @Override
            public Icon getIcon() {
                return ObjectUtils.notNull(null, AllIcons.Actions.IntentionBulb);
            }

            @Override
            public void execute() {
                self.createPypendencyPython(editor, file);
            }
        });

        return new GotoData(PsiUtilCore.getElementAtOffset(file, editor.getCaretModel().getOffset()), PsiElement.EMPTY_ARRAY, actions);
    }

    private PsiFile getPypendencyDefinition(PsiFile file) {
        VirtualFile diPath = this.getDIPath(file);
        if (diPath == null) return null;
        String relativePath = VfsUtilCore.getRelativePath(file.getParent().getVirtualFile(), diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;
        String yamlName = file.getName().replace(".py", ".yaml");

        String diFile = diNewPath + "/" + yamlName;
        if (FileUtil.exists(diFile)) {
            return PsiManager.getInstance(file.getProject()).findFile(
                    LocalFileSystem.getInstance().findFileByPath(diFile)
            );
        }

        diFile = diNewPath + "/" + file.getName();
        if (FileUtil.exists(diFile)) {
            return PsiManager.getInstance(file.getProject()).findFile(
                    LocalFileSystem.getInstance().findFileByPath(diFile)
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

        String fqn = this.currentFQN;
        PsiFile psiFile = PsiFileFactory.getInstance(file.getProject()).createFileFromText(
                file.getName().replace(".py", ".yaml"), YAMLFileType.YML, fqn + ":\n    fqn: " + fqn);

        PsiFile new_file = WriteAction.compute(
                () -> (PsiFile) directory.add(psiFile)
        );
        FileEditorManager.getInstance(editorProject).openFile(new_file.getVirtualFile(), true);
    }

    private void createPypendencyPython(Editor editor, PsiFile file) {
        VirtualFile diPath = this.getDIPath(file);

        if (diPath == null) return;

        String relativePath = VfsUtilCore.getRelativePath(file.getParent().getVirtualFile(), diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;

        PsiDirectory directory = WriteAction.compute(
                () -> DirectoryUtil.mkdirs(PsiManager.getInstance(editor.getProject()), diNewPath)
        );

        String fqn = this.currentFQN;
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
        FileEditorManager.getInstance(editor.getProject()).openFile(new_file.getVirtualFile(), true);
    }

    private @Nullable VirtualFile getDIPath(@NotNull PsiFile file) {
        PsiDirectory directory = file.getParent();

        while (directory != null) {
            String directoryPath = directory.getVirtualFile().getCanonicalPath();

            if (FileUtil.exists(directoryPath + "/_dependency_injection/")) {
                return LocalFileSystem.getInstance().findFileByPath(directoryPath + "/_dependency_injection/");
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
