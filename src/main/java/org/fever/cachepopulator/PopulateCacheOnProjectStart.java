package org.fever.cachepopulator;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.PythonFileType;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.fever.ResolutionCache;
import org.fever.filefinder.DependencyInjectionFilesFinder;
import org.fever.notifier.PypendencyNotifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PopulateCacheOnProjectStart implements ProjectActivity {
    private record DependencyInjectionFileType(FileType fileType, String[] identifierRegexes) {
    }

    private static final DependencyInjectionFileType[] FILE_TYPES = {
        new DependencyInjectionFileType(YAMLFileType.YML, new String[]{ "^(\\S+):\n\\s*fqn:" }),
        new DependencyInjectionFileType(
            PythonFileType.INSTANCE,
            new String[]{
                "container(?:_builder)?\\.set\\(\\s*\"(\\S+)",
                "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"(\\S+)"
            }
        )
    };

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
        PsiManager psiManager = PsiManager.getInstance(project);
        String projectName = project.getName();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(psiManager.getProject());
        int initialNumberOfCachedIdentifiers = resolutionCache.countIdentifiers(projectName);

        for (DependencyInjectionFileType fileType : FILE_TYPES) {
            Collection<VirtualFile> dependencyInjectionFiles = ReadAction.compute(
                () -> DependencyInjectionFilesFinder.find(fileType.fileType(), scope)
            );

            for (String regex : fileType.identifierRegexes()) {
                Matcher matcher = Pattern.compile(regex).matcher("");

                for (VirtualFile file : dependencyInjectionFiles) {
                    PsiFile psiFile = ReadAction.compute(() -> psiManager.findFile(file));

                    if (psiFile == null) {
                        continue;
                    }

                    cacheAllIdentifiersDefinedInFile(file, psiFile, matcher, projectName);
                }
            }
        }

        int currentNumberOfCachedIdentifiers = resolutionCache.countIdentifiers(projectName);
        if (initialNumberOfCachedIdentifiers == 0 && currentNumberOfCachedIdentifiers != 0) {
            String message = "Populated the Pypendency cache for %s with %d identifiers".formatted(projectName,
                                                                                                   currentNumberOfCachedIdentifiers);
            PypendencyNotifier.notify(project, message, NotificationType.INFORMATION);
        }

        return null;
    }

    private static void cacheAllIdentifiersDefinedInFile(VirtualFile file, PsiFile psiFile, Matcher matcher, String projectName) {
        ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
        String fileContent = ReadAction.compute(psiFile::getText);
        matcher.reset(fileContent);

        while (matcher.find()) {
            String identifier = matcher.group(1);
            String cleanIdentifier = identifier.replaceAll("[\"'@,]", "");
            resolutionCache.setCachedResolution(projectName, cleanIdentifier, file.getCanonicalPath());
        }
    }
}
