package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PythonLineMarkerProvider extends LineMarkerProviderDescriptor {
    @Override
    public @Nullable @GutterName String getName() {
        return "Go to dependency injection";
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
        if (diFile == null) {
            return null;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                .create(AllIcons.Graph.Layout)
                .setTarget(diFile)
                .setTooltipText("Navigate to dependency injection file")
                .setAlignment(GutterIconRenderer.Alignment.CENTER);

        return builder.createLineMarkerInfo(pyClass);
    }
}
