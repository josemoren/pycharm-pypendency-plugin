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
        PsiElement file = null;

        if (this.fqnMatchesFileName(fqn)) {
            file = resolveSourceCodeFileFromCurrentDependencyInjectionFile(psiManager);
        }
        if (file == null) {
            file = resolveToFqnsDependencyInjectionFile(fqn, psiManager);
        }
        if (file == null) {
            file = SourceCodeFileResolver.fromFqn(fqn, psiManager);
        }

        return file;
    }

    private boolean fqnMatchesFileName(String fqn) {
        String fqnClassName = fqn.substring(fqn.lastIndexOf(".") + 1);
        String equivalentClassName = CaseFormatter.camelCaseToSnakeCase(fqnClassName) + ".py";
        String fileName = getElement().getContainingFile().getName();
        return equivalentClassName.equals(fileName);
    }

    private @Nullable PsiElement resolveSourceCodeFileFromCurrentDependencyInjectionFile(PsiManager psiManager) {
        String dependencyInjectionFilePath = getElement().getContainingFile().getVirtualFile().getCanonicalPath();
        assert dependencyInjectionFilePath != null;
        return SourceCodeFileResolver.fromDependencyInjectionFilePath(dependencyInjectionFilePath, psiManager);
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

    private String getAbsoluteDependencyInjectionFilePathWithoutExtension(String fqn) {
        // Input: core.infrastructure.user.finders.core_user_finder.CoreUserFinder
        // Output: core/_dependency_injection/infrastructure/user/finders/core_user_finder

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
