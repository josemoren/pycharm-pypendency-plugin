package org.fever.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

public abstract class ReferenceProvider extends PsiReferenceProvider {
    public static final String IDENTIFIER_REGEX_FOR_DI_FILES = "^@[a-z0-9_.]+\\.[A-Za-z0-9_]+$";

    @NotNull
    protected String cleanText(String text) {
        return text.replaceAll("[\"']|\\n+\\s*", "");
    }

    @NotNull
    protected static org.fever.PsiReference getReferenceForIdentifier(@NotNull PsiElement element, String identifier) {
        TextRange range = new TextRange(1, element.getTextRangeInParent().getLength() - 1);
        return new org.fever.PsiReference(element, range, identifier);
    }
}
