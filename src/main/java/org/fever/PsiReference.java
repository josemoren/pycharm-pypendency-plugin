package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.fever.utils.CaseFormatter;
import org.fever.utils.SourceCodeFileResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PsiReference extends PsiReferenceBase<PsiElement> {
    private final String fqn;

    public PsiReference(@NotNull PsiElement element, TextRange textRange, String fqn) {
        super(element, textRange);

        this.fqn = this.cleanFqn(fqn);
    }

    private String cleanFqn(String fqn) {
        return fqn.replaceAll("[\"'@]", "");
    }

    @Override
    public @Nullable PsiElement resolve() {
        PsiManager psiManager = getElement().getManager();
        PsiElement file = resolveToFqnsDependencyInjectionFile(fqn, psiManager);

        if (file == null) {
            file = SourceCodeFileResolver.fromFqn(fqn, psiManager);
        }

        return file;
    }

    private @Nullable PsiElement resolveToFqnsDependencyInjectionFile(String fqn, PsiManager psiManager) {
        String diFilePathWithoutExtension = getAbsoluteDependencyInjectionFilePathWithoutExtension(fqn);

        String[] possibleFilePathsOrderedByMostCommon = {
                diFilePathWithoutExtension + "/" + SourceCodeFileResolver.getClassNameInSnakeCase(fqn) + ".yaml",
                diFilePathWithoutExtension + "/" + SourceCodeFileResolver.getClassNameInSnakeCase(fqn) + ".py",
                diFilePathWithoutExtension + ".yaml",
                diFilePathWithoutExtension + ".py",
                diFilePathWithoutExtension + "/" + SourceCodeFileResolver.getClassNameInSnakeCase(fqn) + ".yml",
                diFilePathWithoutExtension + ".yml",
        };

        for (String filePath : possibleFilePathsOrderedByMostCommon) {
            PsiFile file = SourceCodeFileResolver.getFileFromAbsolutePath(filePath, psiManager);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * Gets the absolute file path of the DI file without the file extension.
     * For example:
     *     fqn: core.infrastructure.user.finders.core_user_finder.CoreUserFinder
     *     return: <PROJECT_PATH>/src/core/_dependency_injection/infrastructure/user/finders/core_user_finder
     * @param fqn full qualified name of the class.
     * @return string with the full path of the DI file without the file extension.
     */
    private String getAbsoluteDependencyInjectionFilePathWithoutExtension(String fqn) {
        String absoluteBasePath = getElement().getProject().getBasePath();
        String[] parts = fqn.split("\\.");
        String djangoAppName = parts[0];
        String relativeFilePath = String.join("/", Arrays.copyOfRange(parts, 1, parts.length - 1));

        return absoluteBasePath + "/src/" + djangoAppName + GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER + relativeFilePath;
    }

    @Override
    public @NotNull String getCanonicalText() {
        return fqn;
    }
}
