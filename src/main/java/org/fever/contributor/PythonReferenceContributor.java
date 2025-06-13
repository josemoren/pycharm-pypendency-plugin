package org.fever.contributor;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import org.fever.provider.PythonReferenceProvider;

public class PythonReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PsiLiteralValue.class)
                        .inFile(PlatformPatterns.psiFile()
                        ),
                new PythonReferenceProvider()
        );
    }
}
