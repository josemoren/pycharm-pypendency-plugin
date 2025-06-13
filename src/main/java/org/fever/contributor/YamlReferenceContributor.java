package org.fever.contributor;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import org.fever.provider.YamlReferenceProvider;

public class YamlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement()
                .inFile(PlatformPatterns.psiFile()
                ),
                new YamlReferenceProvider()
        );
    }
}
