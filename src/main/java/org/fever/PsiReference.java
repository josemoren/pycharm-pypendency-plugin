package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.fever.utils.CaseFormatter;
import org.fever.utils.SourceCodeFileResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PsiReference extends PsiReferenceBase<PsiElement> {
    private static final String[] DI_FILE_EXTENSIONS = { ".yaml", ".py", ".yml" };

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
        PsiElement sourceCodeFile = SourceCodeFileResolver.fromFqn(fqn, psiManager);
        if (sourceCodeFile == null && this.fqnMatchesFileName(fqn)) {
            sourceCodeFile = resolveSourceCodeFileFromCurrentDependencyInjectionFile(psiManager);
        }
        if (sourceCodeFile == null) {
            sourceCodeFile = resolveToFqnsDependencyInjectionFile(fqn, psiManager);
        }

        return sourceCodeFile;
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

        for (String extension : DI_FILE_EXTENSIONS) {
            String absoluteDependencyInjectionFilePath = diFilePathWithoutExtension + extension;
            PsiFile file = SourceCodeFileResolver.getFileFromAbsolutePath(absoluteDependencyInjectionFilePath, psiManager);
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
