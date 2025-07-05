package org.fever.provider;

import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.fever.fileresolver.SourceCodeFileResolverByFqn;
import org.fever.utils.FqnExtractor;
import org.fever.utils.IconCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static org.fever.codeInsight.GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER;

public class PythonDILineMarkerProvider extends LineMarkerProviderDescriptor {
    private static final Icon ICON = IconCreator.create("icons/goToSource.svg");
    public static final String PYTHON_FQN_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*$";

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
        if (file == null || !file.getVirtualFile().getPath().contains(DEPENDENCY_INJECTION_FOLDER)) {
            return null;
        }

        if (!(psiElement instanceof PyStringLiteralExpression stringLiteral)) {
            return null;
        }

        String fqn = stringLiteral.getStringValue().strip();
        if (fqn.isEmpty()) {
            return null;
        }

        if (!isFqnString(fqn, file)) {
            return null;
        }

        PsiFile sourceCodeFile = SourceCodeFileResolverByFqn.resolve(fqn, psiElement.getManager());
        if (sourceCodeFile == null) {
            return null;
        }

        return NavigationGutterIconBuilder.create(ICON)
                                          .setTarget(sourceCodeFile)
                                          .setTooltipText("Navigate to Python class")
                                          .setAlignment(GutterIconRenderer.Alignment.CENTER)
                                          .createLineMarkerInfo(psiElement);
    }

    private static boolean isFqnString(String stringValue, PsiFile file) {
        String extractedFqn = FqnExtractor.extractFqnFromDIFile(file);

        if (!stringValue.equals(extractedFqn)) {
            return false;
        }

        return stringValue.matches(PYTHON_FQN_REGEX);
    }
}
