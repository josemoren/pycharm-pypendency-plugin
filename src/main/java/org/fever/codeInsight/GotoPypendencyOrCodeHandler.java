package org.fever.codeInsight;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.icons.AllIcons;
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
import com.jetbrains.python.psi.PyClass;
import org.fever.ResolutionCache;
import org.fever.filecreator.*;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.fever.utils.PyClassUnderCaretFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        PyClass pyClassUnderCaret = PyClassUnderCaretFinder.find(editor, file);
        String fqn = pyClassUnderCaret.getQualifiedName();

        PsiFile pypendencyDefinitionFile = DependencyInjectionFileResolverByIdentifier.resolve(file.getManager(), fqn);

        if (pypendencyDefinitionFile != null) {
            return getGotoDataForExistingPypendency(pyClassUnderCaret, pypendencyDefinitionFile);
        }

        return getGotoDataForNewPypendency(editor, file, pyClassUnderCaret, this);
    }

    @NotNull
    private GotoTargetHandler.GotoData getGotoDataForExistingPypendency(PsiElement pyClassUnderCaret, PsiFile pypendencyDefinition) {
        PsiElement[] targets = new PsiElement[]{pypendencyDefinition};
        return new GotoData(
                pyClassUnderCaret,
                targets,
                new SmartList<>()
        );
    }

    @NotNull
    private GotoTargetHandler.GotoData getGotoDataForNewPypendency(Editor editor, PsiFile file, PsiElement pyClassUnderCaret, GotoPypendencyOrCodeHandler self) {
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
                self.createAndOpenDIFIle(editor, file, DIFileType.YAML);
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
                self.createAndOpenDIFIle(editor, file, DIFileType.PYTHON);
            }
        });

        return new GotoData(pyClassUnderCaret, PsiElement.EMPTY_ARRAY, actions);
    }

    private void createAndOpenDIFIle(Editor editor, PsiFile file, DIFileType type) {
        PsiDirectory directory = makePypendencyDirectoryForFile(file);
        PyClass targetPyClass = PyClassUnderCaretFinder.find(editor, file);
        if (targetPyClass == null) {
            return;
        }
        String fqn = targetPyClass.getQualifiedName();
        PsiFile dependencyInjectionFile = DIFileCreator.create(targetPyClass, fqn, type);

        PsiFile newFile;
        try {
            newFile = WriteAction.compute(() -> (PsiFile) directory.add(dependencyInjectionFile));
        } catch (Exception e) {
            newFile = directory.findFile(dependencyInjectionFile.getName());
        }

        String createdFilePath = newFile.getVirtualFile().getCanonicalPath();
        ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
        resolutionCache.setCachedResolution(file.getProject().getName(), fqn, createdFilePath);
        Project fileProject = file.getProject();
        FileEditorManager.getInstance(fileProject).openFile(newFile.getVirtualFile(), true);

    }
    private PsiDirectory makePypendencyDirectoryForFile(PsiFile file) {
        VirtualFile diPath = getDIPath(file);
        assert diPath != null;

        PsiDirectory fileParent = file.getParent();
        assert fileParent != null;

        VirtualFile parentVirtualFile = fileParent.getVirtualFile();
        String relativePath = VfsUtilCore.getRelativePath(parentVirtualFile, diPath.getParent());
        String diNewPath = diPath.getCanonicalPath() + "/" + relativePath;

        Project fileProject = file.getProject();
        return WriteAction.compute(
                () -> DirectoryUtil.mkdirs(PsiManager.getInstance(fileProject), diNewPath)
        );
    }

    private static @Nullable VirtualFile getDIPath(@NotNull PsiFile file) {
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
