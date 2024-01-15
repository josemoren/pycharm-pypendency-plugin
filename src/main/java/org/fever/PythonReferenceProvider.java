package org.fever;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.impl.PyReferenceExpressionImpl;
import org.jetbrains.annotations.NotNull;

import static org.fever.GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER;


public class PythonReferenceProvider extends ReferenceProvider {
    private static final String IDENTIFIER_REGEX_FOR_CONTAINER_BUILDER_GET = "^[a-z0-9_.]+\\.[A-Za-z0-9_]+$";

    @Override
    public com.intellij.psi.PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String text = cleanText(element.getText());

        if (isInDependencyInjectionFile(element)) {
            if (text.matches(IDENTIFIER_REGEX_FOR_DI_FILES)) {
                return getReferenceForIdentifierAsArray(element, text);
            }
        } else if (isInContainerBuilderGet(element)) {
            if (text.matches(IDENTIFIER_REGEX_FOR_CONTAINER_BUILDER_GET)) {
                return getReferenceForIdentifierAsArray(element, text);
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }

    private static boolean isInDependencyInjectionFile(@NotNull PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return false;
        }
        String absoluteFilePath = virtualFile.getCanonicalPath();
        if (absoluteFilePath == null) {
            return false;
        }
        return absoluteFilePath.contains(DEPENDENCY_INJECTION_FOLDER);
    }

    private static com.intellij.psi.PsiReference @NotNull [] getReferenceForIdentifierAsArray(@NotNull PsiElement element, String text) {
        PsiReference reference = getReferenceForIdentifier(element, text);
        return new PsiReference[]{reference};
    }
    private boolean isInContainerBuilderGet(PsiElement element) {
        PsiElement grandParent = element.getParent().getParent();
        if (!(grandParent instanceof PyCallExpression)) {
            return false;
        }
        QualifiedName qualifiedName = ((PyReferenceExpressionImpl) grandParent.getFirstChild()).asQualifiedName();
        if (qualifiedName == null) {
            return false;
        }

        return qualifiedName.toString().equals("container_builder.get");
    }
}
