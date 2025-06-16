package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.python.psi.PyClass;
import org.fever.ResolutionCache;
import org.fever.filecreator.DIFileCreator;
import org.fever.filecreator.DIFileType;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.fever.utils.IconCreator;
import org.fever.utils.PyClassUnderCaretFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

import static org.fever.codeInsight.GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER;

public class CreateDILineMarkerProvider implements LineMarkerProvider {
    private static final Icon ICON = IconCreator.create("icons/createDI.svg");

    public @Nullable @GutterName String getName() {
        return "Create dependency injection file";
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PyClass pyClass)) {
            return null;
        }

        String classFqn = pyClass.getQualifiedName();
        if (classFqn == null) {
            return null;
        }

        PsiFile diFile = DependencyInjectionFileResolverByIdentifier.resolve(pyClass.getManager(), classFqn);
        if (diFile != null) { // If the DI file exists, we do not want to create a marker (already exists)
            return null;
        }

        return new LineMarkerInfo<>(
                psiElement,
                psiElement.getTextRange(),
                ICON,
                (e) -> "Create Dependency Injection File",
                new CreateDIGutterNavigationHandler(),
                GutterIconRenderer.Alignment.CENTER,
                () -> "Create Dependency Injection File"
        );
    }

    private static class CreateDIGutterNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
        @Override
        public void navigate(@NotNull MouseEvent e, @NotNull PsiElement element) {
            String classFqn = ((PyClass) element).getQualifiedName();

            assert classFqn != null;
            JBPopupFactory.getInstance()
                    .createListPopup(new DICreationPopupStep(element, classFqn))
                    .show(new RelativePoint(e));
        }
    }

    private static class DICreationPopupStep extends BaseListPopupStep<String> {
        private final PsiElement element;
        private final String classFqn;
        private static final String TITLE = "Choose DI File Format";
        private static final String[] FORMATS = {"YAML", "Python"};

        public DICreationPopupStep(@NotNull PsiElement element, @NotNull String classFqn) {
            super(TITLE, FORMATS);
            this.element = element;
            this.classFqn = classFqn;
        }

        @Override
        public @NotNull String getTextFor(String value) {
            return value + " format";
        }

        @Override
        public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
            PsiFile diFile = null;

            if ("YAML".equals(selectedValue)) {
                diFile = DIFileCreator.create((PyClass) element, classFqn, DIFileType.YAML);
            } else if ("Python".equals(selectedValue)) {
                diFile = DIFileCreator.create((PyClass) element, classFqn, DIFileType.PYTHON);
            }

            if (diFile == null) {
                return FINAL_CHOICE;
            }

            Editor editor = FileEditorManager.getInstance(element.getProject()).getSelectedTextEditor();
            if (editor == null) {
                return FINAL_CHOICE;
            }

            createAndOpenDIFIle(editor, element.getContainingFile(), "YAML".equals(selectedValue) ? DIFileType.YAML : DIFileType.PYTHON);

            return FINAL_CHOICE;
        }

        // Todo: avoid code duplication (create shared class)

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
    }
}
