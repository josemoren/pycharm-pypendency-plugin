package org.fever;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;

public class YamlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile()
                ),
                new ReferenceProvider()
        );
    }
}
