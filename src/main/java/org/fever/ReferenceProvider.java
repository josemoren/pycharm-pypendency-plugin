package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ReferenceProvider extends PsiReferenceProvider {
    public static final String IDENTIFIER_REGEX = "^\"?@?[a-z0-9_.]+\\.[A-Za-z0-9_]+\"?$";

    @Override
    public com.intellij.psi.PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String text = element.getText();
        if (!text.matches(IDENTIFIER_REGEX)) {
            return org.fever.PsiReference.EMPTY_ARRAY;
        }

        org.fever.PsiReference reference = getReferenceForIdentifier(element, text);

        return new org.fever.PsiReference[]{reference};
    }

    @NotNull
    private static org.fever.PsiReference getReferenceForIdentifier(@NotNull PsiElement element, String identifier) {
        TextRange range;
        if (hasQuotes(identifier)) {
             range = new TextRange(1, identifier.length() - 1);
        } else {
            range = new TextRange(0, identifier.length());
        }

        return new org.fever.PsiReference(element, range, identifier);
    }

    private static boolean hasQuotes(String fqn) {
        return fqn.matches("^[\"'].*[\"']$");
    }
}
