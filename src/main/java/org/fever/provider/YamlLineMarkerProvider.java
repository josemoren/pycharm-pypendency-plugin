package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.fever.GotoPypendencyOrCodeHandler;
import org.fever.fileresolver.SourceCodeFileResolverByFqn;
import org.fever.utils.FqnExtractor;
import org.fever.utils.IconCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class YamlLineMarkerProvider extends LineMarkerProviderDescriptor {
    private static final Icon ICON = IconCreator.create("icons/goToSource.svg");

    @Override
    public @Nullable @GutterName String getName() {
        return "Go to python class";
    }

    @Override
    public @Nullable Icon getIcon() {
        return ICON;
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement psiElement) {
        PsiFile file = psiElement.getContainingFile();
        if (file == null || !file.getVirtualFile().getPath().contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER)) {
            return null;
        }

        PsiElement firstElement = file.findElementAt(0);
        if (psiElement != firstElement) {
            return null;
        }

        String fqn = FqnExtractor.extractFqnFromDIFile(file);
        if (fqn == null) {
            return null;
        }

        PsiFile sourceCodeFile = SourceCodeFileResolverByFqn.resolve(fqn, psiElement.getManager());
        if (sourceCodeFile == null) {
            return null;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(ICON)
                .setTarget(sourceCodeFile)
                .setTooltipText("Navigate to Python class")
                .setAlignment(GutterIconRenderer.Alignment.CENTER);

        return builder.createLineMarkerInfo(psiElement);
    }
}
