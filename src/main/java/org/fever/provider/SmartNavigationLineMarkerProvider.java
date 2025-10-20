package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.python.psi.PyClass;
import org.fever.filecreator.DIFileType;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.fever.utils.DIFileOpener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static org.fever.utils.Icons.CREATE_DI_ICON;
import static org.fever.utils.Icons.GO_TO_DI_ICON;

public class SmartNavigationLineMarkerProvider implements LineMarkerProvider {
    public @Nullable @GutterName String getName() {
        return "Smart navigation to dependency injection files";
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

        Collection<PsiFile> diFiles = DependencyInjectionFileResolverByIdentifier.findAll(pyClass.getManager(),
                                                                                          classFqn);

        if (diFiles.isEmpty()) {
            return new LineMarkerInfo<>(
                psiElement,
                psiElement.getTextRange(),
                CREATE_DI_ICON,
                (e) -> "Create Dependency Injection File",
                new CreateDIGutterNavigationHandler(),
                GutterIconRenderer.Alignment.CENTER,
                () -> "Create Dependency Injection File"
            );
        } else if (diFiles.size() == 1) {
            PsiFile diFile = diFiles.iterator().next();
            return new LineMarkerInfo<>(
                psiElement,
                psiElement.getTextRange(),
                GO_TO_DI_ICON,
                (e) -> "Navigate to dependency injection file",
                new DirectNavigationHandler(diFile),
                GutterIconRenderer.Alignment.CENTER,
                () -> "Navigate to dependency injection file"
            );
        } else {
            return new LineMarkerInfo<>(
                psiElement,
                psiElement.getTextRange(),
                GO_TO_DI_ICON,
                (e) -> "Choose dependency injection file",
                new MultipleFilesNavigationHandler(new ArrayList<>(diFiles)),
                GutterIconRenderer.Alignment.CENTER,
                () -> "Choose dependency injection file"
            );
        }
    }

    private static class CreateDIGutterNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
        @Override
        public void navigate(@NotNull MouseEvent e, @NotNull PsiElement element) {
            JBPopupFactory.getInstance()
                .createListPopup(new DICreationPopupStep(element))
                .show(new RelativePoint(e));
        }
    }

    private record DirectNavigationHandler(PsiFile targetFile) implements GutterIconNavigationHandler<PsiElement> {
        @Override
        public void navigate(@NotNull MouseEvent e, @NotNull PsiElement element) {
            FileEditorManager.getInstance(element.getProject())
                .openFile(targetFile.getVirtualFile(), true);
        }
    }

    private record MultipleFilesNavigationHandler(ArrayList<PsiFile> diFiles)
        implements GutterIconNavigationHandler<PsiElement> {
        @Override
        public void navigate(@NotNull MouseEvent e, @NotNull PsiElement element) {
            JBPopupFactory.getInstance()
                .createListPopup(new DIFileSelectionPopupStep(diFiles))
                .show(new RelativePoint(e));
        }
    }

    private static class DICreationPopupStep extends BaseListPopupStep<DIFileType> {
        private final PsiElement element;
        private static final String TITLE = "Choose DI File Format";

        public DICreationPopupStep(@NotNull PsiElement element) {
            super(TITLE, DIFileType.values());
            this.element = element;
        }

        @Override
        public @NotNull String getTextFor(DIFileType value) {
            return value.getName() + " File";
        }

        @Override
        public @Nullable PopupStep<?> onChosen(DIFileType selectedValue, boolean finalChoice) {
            Editor editor = FileEditorManager.getInstance(element.getProject()).getSelectedTextEditor();

            if (editor == null) {
                return FINAL_CHOICE;
            }

            DIFileOpener.open(editor, element.getContainingFile(), selectedValue);
            return FINAL_CHOICE;
        }

        @Override
        public Icon getIconFor(DIFileType value) {
            return value.getIcon();
        }
    }

    private static class DIFileSelectionPopupStep extends BaseListPopupStep<PsiFile> {
        private static final String TITLE = "Choose Dependency Injection File";

        public DIFileSelectionPopupStep(ArrayList<PsiFile> diFiles) {
            super(TITLE, diFiles);
        }

        @Override
        public @NotNull String getTextFor(PsiFile file) {
            String fileName = file.getName();
            String[] pathParts = file.getVirtualFile().getPath().split("/_dependency_injection/");
            String context = pathParts[0].substring(pathParts[0].lastIndexOf('/') + 1).replace("/", "");
            String dddLayer = pathParts[1].substring(0, pathParts[1].indexOf('/') + 1).replace("/", "");

            return String.format("(%s) [%s] %s", context, dddLayer, fileName);
        }

        @Override
        public Icon getIconFor(PsiFile file) {
            String extension = Objects.requireNonNull(file.getVirtualFile().getExtension());
            return DIFileType.fromExtension(extension).getIcon();
        }

        @Override
        public @Nullable PopupStep<?> onChosen(PsiFile selectedFile, boolean finalChoice) {
            FileEditorManager.getInstance(selectedFile.getProject())
                .openFile(selectedFile.getVirtualFile(), true);
            return FINAL_CHOICE;
        }
    }
}
