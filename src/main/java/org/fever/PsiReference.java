package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.fever.utils.SourceCodeFileResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PsiReference extends PsiReferenceBase<PsiElement> {
    private final String identifier;

    public PsiReference(@NotNull PsiElement element, TextRange textRange, String identifier) {
        super(element, textRange);

        this.identifier = this.cleanIdentifier(identifier);
    }

    private String cleanIdentifier(String identifier) {
        return identifier.replaceAll("[\"'@]", "");
    }

    @Override
    public @Nullable PsiElement resolve() {
        PsiManager psiManager = getElement().getManager();
        PsiElement file = resolveToDependencyInjectionFileFromIdentifier(identifier, psiManager);

        if (file == null) {
            file = tryFallingBackToSourceCodeFileUsingIdentifierAsFqn(identifier, psiManager);
        }

        return file;
    }

    private @Nullable PsiElement resolveToDependencyInjectionFileFromIdentifier(String identifier, PsiManager psiManager) {
        String diFileDirectory = getAbsoluteDependencyInjectionFileDirectory(identifier);
        String diFileName = SourceCodeFileResolver.getClassNameInSnakeCase(identifier);

        String[] possibleFilePathsOrderedByMostCommon = {
                diFileDirectory + "/" + diFileName + ".yaml",
                diFileDirectory + "/" + diFileName + ".py",
                diFileDirectory + ".yaml",
                diFileDirectory + ".py",
                diFileDirectory + "/" + diFileName + ".yml",
                diFileDirectory + ".yml",
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
     * identifier: core.infrastructure.user.finders.core_user_finder.CoreUserFinder
     * return: <PROJECT_PATH>/src/core/_dependency_injection/infrastructure/user/finders/core_user_finder
     *
     * @param identifier full qualified name of the class.
     * @return string with the full path of the DI file without the file extension.
     */
    private String getAbsoluteDependencyInjectionFileDirectory(String identifier) {
        String absoluteBasePath = getElement().getProject().getBasePath();
        String[] parts = identifier.split("\\.");
        String djangoAppName = parts[0];
        String relativeFilePath = String.join("/", Arrays.copyOfRange(parts, 1, parts.length - 1));

        return absoluteBasePath + "/src/" + djangoAppName + GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER + relativeFilePath;
    }

    public static @Nullable PsiFile tryFallingBackToSourceCodeFileUsingIdentifierAsFqn(String identifier, PsiManager psiManager) {
        return SourceCodeFileResolver.fromFqn(identifier, psiManager);
    }

    @Override
    public @NotNull String getCanonicalText() {
        return identifier;
    }
}
