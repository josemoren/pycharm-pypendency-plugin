package org.fever.cachepopulator;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.PythonFileType;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.fever.GotoPypendencyOrCodeHandler;
import org.fever.ResolutionCache;
import org.fever.notifier.PypendencyNotifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class PopulateCacheOnProjectStart implements ProjectActivity {
    private record DependencyInjectionFileType(FileType fileType, String[] identifierRegexes) {}
    private static final DependencyInjectionFileType[] FILE_TYPES = {
            new DependencyInjectionFileType(
                    YAMLFileType.YML,
                    new String[]{
                            "^(\\S+):\n\\s*fqn:"
                    }),
            new DependencyInjectionFileType(
                    PythonFileType.INSTANCE,
                    new String[]{
                            "container(?:_builder)?\\.set\\(\\s*\"(\\S+)",
                            "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"(\\S+)"
                    })
    };

    private static final ResolutionCache.State resolutionCache = ResolutionCache.getInstance();

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        PsiManager psiManager = PsiManager.getInstance(project);
        String projectName = project.getName();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(psiManager.getProject());
        int initialNumberOfCachedIdentifiers = resolutionCache.countIdentifiers(projectName);

        for (DependencyInjectionFileType fileType : FILE_TYPES) {
            Collection<VirtualFile> dependencyInjectionFiles = getDependencyInjectionFiles(scope, fileType.fileType());
            for (String regex : fileType.identifierRegexes()) {
                Matcher matcher = Pattern.compile(regex).matcher("");
                for (VirtualFile file : dependencyInjectionFiles) {
                    PsiFile psiFile = ReadAction.compute(() -> psiManager.findFile(file));
                    if (psiFile == null) {
                        continue;
                    }
                    String fileContent = ReadAction.compute(psiFile::getText);
                    if (!matcher.reset(fileContent).find()) {
                        continue;
                    }
                    String identifier = matcher.group(1);
                    String cleanIdentifier = identifier.replaceAll("[\"'@,]", "");
                    resolutionCache.setCachedResolution(projectName, cleanIdentifier, file.getCanonicalPath());
                }
            }
        }

        int currentNumberOfCachedIdentifiers = resolutionCache.countIdentifiers(projectName);
        if (initialNumberOfCachedIdentifiers == 0 && currentNumberOfCachedIdentifiers != 0) {
            String message = "Populated the Pypendency cache for " + projectName + " with " + currentNumberOfCachedIdentifiers + " identifiers";
            PypendencyNotifier.notify(project, message, NotificationType.INFORMATION);
        }

        return null;
    }

    private static Collection<VirtualFile> getDependencyInjectionFiles(GlobalSearchScope scope, FileType fileType) {
        return ReadAction.compute(() -> FileTypeIndex.getFiles(fileType, scope).stream()
                .filter(file -> file.getPath().contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER))
                .toList());
    }
}
