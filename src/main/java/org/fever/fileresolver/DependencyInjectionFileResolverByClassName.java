package org.fever.fileresolver;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.fever.ResolutionCache;

import java.util.ArrayList;
import java.util.Collection;

public class DependencyInjectionFileResolverByClassName {
    private static final ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
    private static final Logger LOG = Logger.getInstance("Pypendency");

    private static PsiFile resolveFromCache(PsiManager manager, String identifier) {
        String projectName = manager.getProject().getName();
        String cachedFilePath = resolutionCache.getCachedResolution(projectName, identifier);
        if (cachedFilePath == null) {
            LOG.info("No cached resolution found for " + identifier);
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

    public static PsiFile resolve(Project project, String className) {
        PsiManager psiManager = PsiManager.getInstance(project);
        String projectName = project.getName();
        Collection<String> possibleIdentifiers = resolutionCache.getCachedIdentifiersByClassName(projectName, className);
        LOG.info("Possible identifiers found for class " + className + ": " + String.join("\n\t", possibleIdentifiers));
        ArrayList<PsiFile> possibleDIFiles = new ArrayList<>();
        for (String identifier : possibleIdentifiers) {
            PsiFile fileRetrievedFromCachedPath = resolveFromCache(psiManager, identifier);
            if (fileRetrievedFromCachedPath != null) {
                possibleDIFiles.add(fileRetrievedFromCachedPath);
            }
        }

        // TODO: in the future, return all the possible DI files and show them in a list
        return possibleDIFiles.stream().findFirst().orElse(null);
    }

}
