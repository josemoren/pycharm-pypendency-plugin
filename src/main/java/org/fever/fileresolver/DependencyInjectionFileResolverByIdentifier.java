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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyInjectionFileResolverByIdentifier {
    private static final String[] REGEX_FOR_PYTHON_MANUALLY_SET_IDENTIFIERS = {
            "container(?:_builder)?\\.set\\(\\s*\"(\\S+)\"",
            "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"(\\S+)\"",
    };
    private static final String REGEX_FOR_YAML_DI_FILES = "^(\\S+):\n\\s*fqn:";

    // Regex patterns for finding files that contain FQN in their definitions
    private static final String YAML_FQN_CONTENT_REGEX = "fqn:\\s*(\\S+)";
    private static final String PYTHON_FQN_CONTENT_REGEX = "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"[^\"]+\",\\s*\"([^\"]+)\"";

    private static final Logger LOG = Logger.getInstance("Pypendency");

    /**
     * Given an identifier and a project, get the corresponding DI file absolute path. Try to find it in the cache,
     * and if it's not present, resolve it manually and store it in the cache.
     * For example:
     * identifier: core.infrastructure.user.finders.core_user_finder.CoreUserFinder
     * return: <PROJECT_PATH>/src/core/_dependency_injection/infrastructure/user/finders/core_user_finder
     *
     * @param identifier full qualified name of the class.
     *
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

        PsiFile fileRetrievedFromCachedPath = SourceCodeFileResolverByFqn.getFileFromAbsolutePath(cachedFilePath,
                                                                                                  manager);
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
        Collection<VirtualFile> yamlDependencyInjectionFiles = DependencyInjectionFilesFinder.find(YAMLFileType.YML,
                                                                                                   scope);
        PsiFile yamlDependencyInjectionFile = findDependencyInjectionFileInCollection(REGEX_FOR_YAML_DI_FILES,
                                                                                      yamlDependencyInjectionFiles,
                                                                                      psiManager, identifier);
        if (yamlDependencyInjectionFile != null) {
            return yamlDependencyInjectionFile;
        }

        Collection<VirtualFile> pythonDependencyInjectionFiles = DependencyInjectionFilesFinder.find(
                PythonFileType.INSTANCE, scope);

        for (String regex : REGEX_FOR_PYTHON_MANUALLY_SET_IDENTIFIERS) {
            PsiFile pythonDependencyInjectionFile = findDependencyInjectionFileInCollection(regex,
                                                                                            pythonDependencyInjectionFiles,
                                                                                            psiManager, identifier);
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

    /**
     * Find all dependency injection files for a given identifier/FQN.
     * This method returns all matching files instead of just the first one.
     * It searches for both:
     * 1. Files that DEFINE services with the identifier as service name
     * 2. Files that DEFINE services with the identifier as FQN content
     *
     * @param psiManager the PSI manager
     * @param identifier the FQN to search for
     * @return collection of all matching DI files
     */
    public static Collection<PsiFile> findAll(PsiManager psiManager, String identifier) {
        String cleanIdentifier = identifier.replaceAll("[\"'@]", "");
        Set<PsiFile> allFiles = new HashSet<>();

        GlobalSearchScope scope = GlobalSearchScope.projectScope(psiManager.getProject());

        // Find all YAML DI files
        Collection<VirtualFile> yamlDependencyInjectionFiles = DependencyInjectionFilesFinder.find(YAMLFileType.YML, scope);

        // 1. Find files that DEFINE the identifier
        allFiles.addAll(findAllDependencyInjectionFilesInCollection(REGEX_FOR_YAML_DI_FILES,
                                                                    yamlDependencyInjectionFiles,
                                                                    psiManager, cleanIdentifier));

        // 2. Find files that contain the FQN in their definitions
        allFiles.addAll(findAllDependencyInjectionFilesInCollection(YAML_FQN_CONTENT_REGEX,
                                                                    yamlDependencyInjectionFiles,
                                                                    psiManager, cleanIdentifier));

        // Find all Python DI files
        Collection<VirtualFile> pythonDependencyInjectionFiles = DependencyInjectionFilesFinder.find(PythonFileType.INSTANCE, scope);

        // 1. Find files that DEFINE the identifier
        for (String regex : REGEX_FOR_PYTHON_MANUALLY_SET_IDENTIFIERS) {
            allFiles.addAll(findAllDependencyInjectionFilesInCollection(regex,
                                                                        pythonDependencyInjectionFiles,
                                                                        psiManager, cleanIdentifier));
        }

        // 2. Find files that contain the FQN in their definitions
        allFiles.addAll(findAllDependencyInjectionFilesInCollection(PYTHON_FQN_CONTENT_REGEX,
                                                                    pythonDependencyInjectionFiles,
                                                                    psiManager, cleanIdentifier));

        return allFiles;
    }

    /**
     * Find all dependency injection files in a collection that match the given identifier.
     *
     * @param regex the regex pattern to match
     * @param diFiles the collection of DI files to search
     * @param psiManager the PSI manager
     * @param identifier the identifier to search for
     * @return collection of all matching DI files
     */
    private static Collection<PsiFile> findAllDependencyInjectionFilesInCollection(String regex, Collection<VirtualFile> diFiles, PsiManager psiManager, String identifier) {
        Collection<PsiFile> matchingFiles = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher("");

        for (VirtualFile virtualFile : diFiles) {
            PsiFile diFile = psiManager.findFile(virtualFile);

            if (diFile == null) {
                continue;
            }

            String diFileContent = diFile.getText();

            if (identifierIsInFile(diFileContent, identifier, matcher, regex)) {
                matchingFiles.add(diFile);
            }
        }
        return matchingFiles;
    }

    /**
     * Check if identifier is present in a DI file, using different logic for definition vs usage patterns.
     *
     * @param diFileContent the content of the DI file
     * @param identifier the identifier to search for
     * @param matcher the regex matcher
     * @param regex the regex pattern being used
     * @return true if identifier is found in the file
     */
    private static boolean identifierIsInFile(String diFileContent, String identifier, Matcher matcher, String regex) {
        // For FQN content patterns, we need to search for the identifier in the matched FQN
        if (regex.equals(YAML_FQN_CONTENT_REGEX) ||
            regex.equals(PYTHON_FQN_CONTENT_REGEX)) {
            return identifierIsInFqnContent(diFileContent, identifier, matcher);
        }

        // For definition patterns, use the original logic
        return identifierIsDefinedInFile(diFileContent, identifier, matcher);
    }

    /**
     * Check if identifier is present in the FQN content of a DI file.
     *
     * @param diFileContent the content of the DI file
     * @param identifier the identifier to search for
     * @param matcher the regex matcher
     * @return true if identifier is found in the FQN content of the file
     */
    private static boolean identifierIsInFqnContent(String diFileContent, String identifier, Matcher matcher) {
        matcher.reset(diFileContent);

        while (matcher.find()) {
            String fqnContent = matcher.group(1);
            // Check if the FQN content matches our identifier
            if (fqnContent.equals(identifier)) {
                return true;
            }
        }

        return false;
    }
}
