package org.fever;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

public class PythonReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PsiLiteralValue.class)
                .inFile(PlatformPatterns.psiFile()
                .withName(PlatformPatterns.string().endsWith(".py"))
                ),
                new ReferenceProvider()
                );
    }
}
