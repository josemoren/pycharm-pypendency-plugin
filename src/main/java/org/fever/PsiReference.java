package org.fever;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.PythonFileType;
import org.fever.utils.CaseFormatter;
import org.fever.utils.SourceCodeFileResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PsiReference extends PsiReferenceBase<PsiElement> {
    private final String fqn;
    private static final String[] MANUALLY_SET_FQN_GROUP_SELECTOR_REGEXPS = {
            "container_builder\\.set\\(\\s*\"(\\S+)\"",
            "container_builder\\.set_definition\\(\\s*Definition\\(\\s*\"(\\S+)\"",
    };

    public PsiReference(@NotNull PsiElement element, TextRange textRange, String fqn) {
        super(element, textRange);

        this.fqn = this.cleanFqn(fqn);
    }

    private String cleanFqn(String fqn) {
        return fqn.replaceAll("[\"'@]", "");
    }

    @Override
    public @Nullable PsiElement resolve() {
        PsiElement thisElement = getElement();
        PsiManager psiManager = thisElement.getManager();
        PsiElement file = null;

        if (this.fqnMatchesFileName(fqn)) {
            file = resolveSourceCodeFileFromCurrentDependencyInjectionFile(psiManager);
        }
        if (file == null) {
            file = resolveToFqnsDependencyInjectionFile(fqn, psiManager);
        }
        if (file == null) {
            file = resolveToDependencyInjectionManualDeclaration(fqn, psiManager);
        }
        if (file == null) {
            file = SourceCodeFileResolver.fromFqn(fqn, psiManager);
        }
        if (file == null) {
            file = thisElement.getContainingFile();
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

    private PsiElement resolveToDependencyInjectionManualDeclaration(String fqn, PsiManager psiManager) {
        GlobalSearchScope scope = new DependencyInjectionSearchScope(getElement().getProject());
        Collection<VirtualFile> dependencyInjectionFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        FileTypeIndex.NAME,
                        PythonFileType.INSTANCE,
                        scope);

        for (String regex : MANUALLY_SET_FQN_GROUP_SELECTOR_REGEXPS) {
            Matcher matcher = Pattern.compile(regex).matcher("");
            for (VirtualFile file : dependencyInjectionFiles) {
                PsiFile psiFile = psiManager.findFile(file);
                assert psiFile != null;
                String fileContent = psiFile.getText();
                matcher.reset(fileContent);
                if (matcher.find() && matcher.group(1).equals(fqn)) {
                    return psiFile;
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull String getCanonicalText() {
        return fqn;
    }
}
