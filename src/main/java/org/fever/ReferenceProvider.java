package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

public abstract class ReferenceProvider extends PsiReferenceProvider {
    @NotNull
    protected String cleanText(String text) {
        return text.replaceAll("[\"']", "");
    }

    @NotNull
    protected static org.fever.PsiReference getReferenceForIdentifier(@NotNull PsiElement element, String identifier) {
        TextRange range;
        range = new TextRange(1, identifier.length() + 1);
        return new org.fever.PsiReference(element, range, identifier);
    }
}
