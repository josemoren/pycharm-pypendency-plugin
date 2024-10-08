package org.fever.fileresolver;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.PythonFileType;
import org.fever.ResolutionCache;
import org.fever.filefinder.DependencyInjectionFilesFinder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyInjectionFileResolverByIdentifier {
    private static final String[] REGEX_FOR_PYTHON_MANUALLY_SET_IDENTIFIERS = {
            "container(?:_builder)?\\.set\\(\\s*\"(\\S+)\"",
            "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"(\\S+)\"",
    };
    private static final String REGEX_FOR_YAML_DI_FILES = "^(\\S+):\n\\s*fqn:";
    private static final Logger LOG = Logger.getInstance("Pypendency");

    /**
     * Given an identifier and a project, get the corresponding DI file absolute path. Try to find it in the cache,
     * and if it's not present, resolve it manually and store it in the cache.
     * For example:
     * identifier: core.infrastructure.user.finders.core_user_finder.CoreUserFinder
     * return: <PROJECT_PATH>/src/core/_dependency_injection/infrastructure/user/finders/core_user_finder
     *
     * @param identifier full qualified name of the class.
     * @return string with the full path of the DI file without the file extension.
     */
    public static @Nullable PsiFile resolve(PsiManager psiManager, String identifier) {
        String cleanIdentifier = identifier.replaceAll("[\"'@]", "");
        PsiFile dependencyInjectionFile = resolveFromCache(psiManager, cleanIdentifier);
        if (dependencyInjectionFile != null) {
            return dependencyInjectionFile;
        }
        return resolveManuallyAndStoreInCache(psiManager, cleanIdentifier);
    }

    private static PsiFile resolveFromCache(PsiManager manager, String identifier) {
        ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
        String projectName = manager.getProject().getName();
        String cachedFilePath = resolutionCache.getCachedResolution(projectName, identifier);
        if (cachedFilePath == null) {
            return null;
        }

        PsiFile fileRetrievedFromCachedPath = SourceCodeFileResolverByFqn.getFileFromAbsolutePath(cachedFilePath, manager);
        if (fileRetrievedFromCachedPath == null) {
            LOG.warn("Cached file not found at " + cachedFilePath + ". Removing from cache.");
            resolutionCache.removeCachedResolution(projectName, identifier);
            return null;
        }

        return fileRetrievedFromCachedPath;
    }

    @Nullable
    private static PsiFile resolveManuallyAndStoreInCache(PsiManager psiManager, String identifier) {
        ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
        PsiFile file = resolveToDependencyInjectionManualDeclaration(identifier, psiManager);

        if (file == null) {
            LOG.info("No manual resolution found for " + identifier);
            return null;
        }

        LOG.info("Storing resolution in cache for " + identifier);
        String filePath = file.getVirtualFile().getPath();
        String projectName = psiManager.getProject().getName();
        resolutionCache.setCachedResolution(projectName, identifier, filePath);
        return file;
    }

    private static @Nullable PsiFile resolveToDependencyInjectionManualDeclaration(String identifier, PsiManager psiManager) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(psiManager.getProject());

        Collection<VirtualFile> yamlDependencyInjectionFiles = DependencyInjectionFilesFinder.find(YAMLFileType.YML, scope);
        PsiFile yamlDependencyInjectionFile = findDependencyInjectionFileInCollection(REGEX_FOR_YAML_DI_FILES, yamlDependencyInjectionFiles, psiManager, identifier);
        if (yamlDependencyInjectionFile != null) {
            return yamlDependencyInjectionFile;
        }

        Collection<VirtualFile> pythonDependencyInjectionFiles = DependencyInjectionFilesFinder.find(PythonFileType.INSTANCE, scope);
        for (String regex : REGEX_FOR_PYTHON_MANUALLY_SET_IDENTIFIERS) {
            PsiFile pythonDependencyInjectionFile = findDependencyInjectionFileInCollection(regex, pythonDependencyInjectionFiles, psiManager, identifier);
            if (pythonDependencyInjectionFile != null) {
                return pythonDependencyInjectionFile;
            }
        }

        return null;
    }

    private static @Nullable PsiFile findDependencyInjectionFileInCollection(String regex, Collection<VirtualFile> diFiles, PsiManager psiManager, String identifier) {
        Matcher matcher = Pattern.compile(regex).matcher("");
        for (VirtualFile virtualFile : diFiles) {
            PsiFile diFile = psiManager.findFile(virtualFile);
            if (diFile == null) {
                continue;
            }
            String diFileContent = diFile.getText();
            if (identifierIsDefinedInFile(diFileContent, identifier, matcher)) {
                return diFile;
            }
        }
        return null;
    }

    private static boolean identifierIsDefinedInFile(String diFileContent, String identifier, Matcher matcher) {
        matcher.reset(diFileContent);
        while (matcher.find()) {
            if (matcher.group(1).equals(identifier)) {
                return true;
            }
        }
        return false;
    }
}
