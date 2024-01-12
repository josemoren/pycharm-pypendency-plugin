package org.fever;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;


public class YamlReferenceProvider extends ReferenceProvider {
    protected static final String IDENTIFIER_REGEX = "^@[a-z0-9_.]+\\.[A-Za-z0-9_]+$";

    @Override
    public com.intellij.psi.PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String text = cleanText(element.getText());
        if (text.matches(IDENTIFIER_REGEX)) {
            PsiReference reference = getReferenceForIdentifier(element, text);
            return new PsiReference[]{reference};
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
