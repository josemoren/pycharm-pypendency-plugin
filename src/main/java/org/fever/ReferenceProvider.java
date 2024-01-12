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
        int offset = element.getText().contains("@") ? 1 : 0;
        range = new TextRange(offset, identifier.length() + offset);
        return new org.fever.PsiReference(element, range, identifier);
    }
}
