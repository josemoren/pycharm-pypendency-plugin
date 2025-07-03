package org.fever.provider;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.fever.PsiReference;
import org.jetbrains.annotations.NotNull;


public class YamlReferenceProvider extends ReferenceProvider {
    @Override
    public com.intellij.psi.PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String text = cleanText(element.getText());

        if (text.matches(IDENTIFIER_REGEX_FOR_DI_FILES)) {
            PsiReference reference = getReferenceForIdentifier(element, text);
            return new PsiReference[]{ reference };
        }

        return PsiReference.EMPTY_ARRAY;
    }
}
