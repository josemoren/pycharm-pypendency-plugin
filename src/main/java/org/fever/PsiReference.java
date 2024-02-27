package org.fever;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.PythonFileType;
import org.fever.utils.SourceCodeFileResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PsiReference extends PsiReferenceBase<PsiElement> {
    private final String identifier;
    private static final String[] REGEX_FOR_MANUALLY_SET_IDENTIFIERS = {
            "container(?:_builder)?\\.set\\(\\s*\"(\\S+)\"",
            "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"(\\S+)\"",
    };
    private final ResolutionCache.State resolutionCache;
    private final String projectName;

    public PsiReference(@NotNull PsiElement element, TextRange textRange, String identifier) {
        super(element, textRange);

        this.identifier = this.cleanIdentifier(identifier);
        this.resolutionCache = ServiceManager.getService(ResolutionCache.class).getState();
        this.projectName = element.getProject().getName();
    }

    private String cleanIdentifier(String identifier) {
        return identifier.replaceAll("[\"'@]", "");
    }

    @Override
    public @Nullable PsiFile resolve() {
        PsiManager psiManager = getElement().getManager();

        PsiFile dependencyInjectionFile = resolveFromCache(psiManager);
        if (dependencyInjectionFile != null) {
            return dependencyInjectionFile;
        }
        return resolveManuallyAndCache(psiManager);
    }

    private PsiFile resolveFromCache(PsiManager psiManager) {
        String filePath = resolutionCache.getCachedResolution(projectName, identifier);
        if (filePath == null) {
            return null;
        }

        PsiFile cachedResult = SourceCodeFileResolver.getFileFromAbsolutePath(filePath, psiManager);
        if (cachedResult == null) {
            resolutionCache.removeCachedResolution(projectName, identifier);
            return null;
        }

        return cachedResult;
    }

    @Nullable
    private PsiFile resolveManuallyAndCache(PsiManager psiManager) {
        PsiFile file = resolveToDependencyInjectionFileFromIdentifier(identifier, psiManager);

        if (file == null) {
            file = resolveToDependencyInjectionManualDeclaration(identifier, psiManager);
        }
        if (file != null) {
            String filePath = file.getVirtualFile().getPath();
            resolutionCache.setCachedResolution(projectName, identifier, filePath);
        }
        if (file == null) {
            file = tryFallingBackToSourceCodeFileUsingIdentifierAsFqn(identifier, psiManager);
        }

        return file;
    }

    private @Nullable PsiFile resolveToDependencyInjectionFileFromIdentifier(String identifier, PsiManager psiManager) {
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

    private PsiFile resolveToDependencyInjectionManualDeclaration(String identifier, PsiManager psiManager) {
        GlobalSearchScope scope = new DependencyInjectionSearchScope(getElement().getProject());
        Collection<VirtualFile> dependencyInjectionFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        FileTypeIndex.NAME,
                        PythonFileType.INSTANCE,
                        scope);

        for (String regex : REGEX_FOR_MANUALLY_SET_IDENTIFIERS) {
            Matcher matcher = Pattern.compile(regex).matcher("");
            for (VirtualFile file : dependencyInjectionFiles) {
                PsiFile psiFile = psiManager.findFile(file);
                assert psiFile != null;
                String fileContent = psiFile.getText();
                matcher.reset(fileContent);
                if (matcher.find() && matcher.group(1).equals(identifier)) {
                    return psiFile;
                }
            }
        }
        return null;
    }

    public static @Nullable PsiFile tryFallingBackToSourceCodeFileUsingIdentifierAsFqn(String identifier, PsiManager psiManager) {
        return SourceCodeFileResolver.fromFqn(identifier, psiManager);
    }

    @Override
    public @NotNull String getCanonicalText() {
        return identifier;
    }
}
