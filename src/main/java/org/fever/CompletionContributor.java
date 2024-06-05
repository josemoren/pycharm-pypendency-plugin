package org.fever;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    private static final String INTELLIJ_DEFAULT_STRING = "IntellijIdeaRulezzz "; // https://intellij-support.jetbrains.com/hc/en-us/community/posts/4411826210066-How-to-deal-with-INTELLIJIDEARULEZZZ-in-Reference-Code-Completion
    private static final ResolutionCache.State resolutionCache = ResolutionCache.getInstance();
    public CompletionContributor() {
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition().getContainingFile().findElementAt(parameters.getOffset());
        if (!isDependencyInjectionStatement(element)) {
            return;
        }
        String text = element.getText()
                .replace(INTELLIJ_DEFAULT_STRING, "")
                .replaceAll("[\"']", "");

        String cleanIdentifier = text.replace("@", "");
        String projectName = parameters.getPosition().getProject().getName();
        List<String> completions = resolutionCache.fuzzyFindIdentifiersMatching(projectName, cleanIdentifier);
        for (String completion : completions) {
            if (!text.startsWith("@") && isInDependencyInjectionFolder(element)) {
                completion = "@" + completion;
            }
            result.addElement(LookupElementBuilder.create(completion).withIcon(AllIcons.Nodes.Annotationtype));
        }
    }

    private boolean isDependencyInjectionStatement(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }

        return isInDependencyInjectionFolder(element) || isInContainerBuilderGetStatement(element);
    }

    private boolean isInDependencyInjectionFolder(@NotNull PsiElement element) {
        PsiDirectory containingDirectory = element.getContainingFile().getOriginalFile().getContainingDirectory();
        if (containingDirectory == null) {
            return false;
        }
        String folderPath = containingDirectory.toString();
        return folderPath.contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER);
    }

    private boolean isInContainerBuilderGetStatement(@NotNull PsiElement element) {
        PyCallExpression callExpression = PsiTreeUtil.getParentOfType(element, PyCallExpression.class);
        if (callExpression == null) {
            return false;
        }

        PyExpression callee = callExpression.getCallee();
        if (callee == null) {
            return false;
        }

        return callee.getText().equals("container_builder.get");
    }
}
