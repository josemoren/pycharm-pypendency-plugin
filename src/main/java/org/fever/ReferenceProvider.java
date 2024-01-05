package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.fever.utils.FqnExtractor;
import org.jetbrains.annotations.NotNull;

public class ReferenceProvider extends PsiReferenceProvider {
    @Override
    public com.intellij.psi.PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String text = element.getText();
        if (!text.matches(FqnExtractor.FQN_REGEX)) {
            return org.fever.PsiReference.EMPTY_ARRAY;
        }

        org.fever.PsiReference reference = getReferenceForFqn(element, text);

        return new org.fever.PsiReference[]{reference};
    }

    @NotNull
    private static org.fever.PsiReference getReferenceForFqn(@NotNull PsiElement element, String fqn) {
        TextRange range;
        if (hasQuotes(fqn)) {
             range = new TextRange(1, fqn.length() - 1);
        } else {
            range = new TextRange(0, fqn.length());
        }

        return new org.fever.PsiReference(element, range, fqn);
    }

    private static boolean hasQuotes(String fqn) {
        return fqn.matches("^[\"'].*[\"']$");
    }
}
