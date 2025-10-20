package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.fever.utils.IconCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PythonLineMarkerProvider extends LineMarkerProviderDescriptor {
    private static final Icon ICON = IconCreator.create("icons/goToDI.svg");

    @Override
    public @Nullable @GutterName String getName() {
        return "Go to dependency injection";
    }

    @Override
    public @Nullable Icon getIcon() {
        return ICON;
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

        return NavigationGutterIconBuilder.create(ICON)
                .setTarget(diFile)
                .setTooltipText("Navigate to dependency injection file")
                .setAlignment(GutterIconRenderer.Alignment.CENTER)
                .createLineMarkerInfo(pyClass);
    }
}
