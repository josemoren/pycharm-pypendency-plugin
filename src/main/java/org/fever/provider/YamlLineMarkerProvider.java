package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.fever.GotoPypendencyOrCodeHandler;
import org.fever.fileresolver.SourceCodeFileResolverByFqn;
import org.fever.utils.FqnExtractor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YamlLineMarkerProvider extends LineMarkerProviderDescriptor {
    @Override
    public @Nullable @GutterName String getName() {
        return "Go to python class";
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
                .create(AllIcons.Graph.Layout)
                .setTarget(sourceCodeFile)
                .setTooltipText("Navigate to Python class")
                .setAlignment(GutterIconRenderer.Alignment.CENTER);

        return builder.createLineMarkerInfo(psiElement);
    }
}
