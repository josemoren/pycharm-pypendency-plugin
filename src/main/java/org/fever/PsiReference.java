package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiReference extends PsiReferenceBase<PsiElement> {
    private final String identifier;

    public PsiReference(@NotNull PsiElement element, TextRange textRange, String identifier) {
        super(element, textRange);
        this.identifier = identifier;
    }

    @Override
    public @Nullable PsiFile resolve() {
        return DependencyInjectionFileResolverByIdentifier.resolve(getElement().getManager(), identifier);
    }

    @Override
    public @NotNull String getCanonicalText() {
        return identifier;
    }
}
