package org.fever.codeInsight;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyParameter;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.fever.fileresolver.DependencyInjectionFileResolverByIdentifier;
import org.fever.fileresolver.SourceCodeFileResolverByFqn;
import org.fever.notifier.PypendencyNotifier;
import org.fever.utils.ClassArgumentParser;
import org.fever.utils.FqnExtractor;
import org.fever.utils.PyClassUnderCaretFinder;
import org.fever.utils.RegexMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static org.fever.utils.FileTypeByExtensionChecker.isPythonFile;
import static org.fever.utils.FileTypeByExtensionChecker.isYamlFile;

public class GotoInjectedImplementationHandler extends GotoTargetHandler {
    public static final String FEATURE_KEY = "navigation.goto.injectedImplementation";
    public static final String NOT_FOUND = "Not found";

    private static final String YAML_ARGUMENT_IDENTIFIER_SELECTOR_REGEX = "(?<=@)[^\"']*";
    private static final String PYTHON_ARGUMENT_IDENTIFIER_SELECTOR_REGEX = "(?<=Argument\\.no_kw_argument\\()[^)]*(?=\\))";

    AnActionEvent anActionEvent;

    public GotoInjectedImplementationHandler(AnActionEvent anActionEvent) {
        super();
        this.anActionEvent = anActionEvent;
    }

    @Override
    protected String getFeatureUsedKey() {
        return FEATURE_KEY;
    }

    @Override
    protected @Nullable GotoData getSourceAndTargetElements(Editor editor, PsiFile file) {
        int caretOffset = editor.getCaretModel().getOffset();

        PsiElement elementUnderCaret = file.findElementAt(caretOffset);
        if (elementUnderCaret == null) {
            return null;
        }

        PsiFile injectedImplementationFile = getInjectedImplementation(editor, elementUnderCaret);
        if (injectedImplementationFile == null) {
            return null;
        }

        PsiElement[] targets = new PsiElement[]{ injectedImplementationFile };
        return new GotoData(elementUnderCaret, targets, new SmartList<>());
    }

    private @Nullable PsiFile getInjectedImplementation(Editor editor, PsiElement elementUnderCaret) {
        PyFile currentFile = (PyFile) elementUnderCaret.getContainingFile();
        Project project = currentFile.getProject();
        PsiManager manager = currentFile.getManager();
        TypeEvalContext context = TypeEvalContext.codeCompletion(project, currentFile.getContainingFile());
        PyFunction initMethod = PyClassUnderCaretFinder.find(editor, currentFile).findInitOrNew(false, context);
        assert initMethod != null;

        PyParameter[] initMethodParameters = initMethod.getParameterList().getParameters();
        String targetClass = elementUnderCaret.getText();
        String fqn = PyClassUnderCaretFinder.find(editor, currentFile).getQualifiedName();
        PsiFile dependencyInjectionFile = DependencyInjectionFileResolverByIdentifier.resolve(manager, fqn);
        String targetIdentifier = getIdentifierFromPosition(dependencyInjectionFile, targetClass, initMethodParameters);

        if (targetIdentifier == null) {
            return null;
        }

        String targetFqn = getFqnFromIdentifier(manager, targetIdentifier);

        PsiFile injectedImplementationSourceCodeFile = SourceCodeFileResolverByFqn.resolve(targetFqn, manager);
        if (injectedImplementationSourceCodeFile == null) {
            String message = "Could not find the source code file associated to the FQN \"" + targetFqn + "\".";
            PypendencyNotifier.notify(project, message, NotificationType.ERROR);
            return null;
        }

        return injectedImplementationSourceCodeFile;
    }

    private String getIdentifierFromPosition(@Nullable PsiFile dependencyInjectionFile, String targetClass, PyParameter[] initMethodParameters) {
        Project project = initMethodParameters[0].getProject();

        if (dependencyInjectionFile == null) {
            String message = "Could not find the injected implementation for \"" + targetClass + "\".\nMake sure that the DI file is correct.";
            PypendencyNotifier.notify(project, message, NotificationType.ERROR);
            return null;
        }

        int targetClassIndexInInit = Arrays.stream(initMethodParameters)
            .filter(parameter -> targetClass.equals(
                ClassArgumentParser.parse(parameter.getText())))
            .findFirst()
            .map(Arrays.asList(initMethodParameters)::indexOf)
            .map(index -> index - 1)
            .orElse(-1);

        if (targetClassIndexInInit < 0) {
            String message = "Could not find the injected implementation for \"" + targetClass + "\".\nCheck that the class is properly type-hinted in the __init__ method.";
            PypendencyNotifier.notify(project, message, NotificationType.ERROR);
            return null;
        }

        String diFileExtension = Objects.requireNonNull(dependencyInjectionFile.getVirtualFile().getExtension());
        String dependencyInjectionFileText = dependencyInjectionFile.getText();
        String[] argumentsInDIFile = {};

        if (isYamlFile(diFileExtension)) {
            argumentsInDIFile = RegexMatcher.getAllMatches(dependencyInjectionFileText,
                                                           YAML_ARGUMENT_IDENTIFIER_SELECTOR_REGEX);
        }

        if (isPythonFile(diFileExtension)) {
            argumentsInDIFile = Pattern.compile(PYTHON_ARGUMENT_IDENTIFIER_SELECTOR_REGEX)
                .matcher(dependencyInjectionFileText)
                .results()
                .map(MatchResult::group)
                .map(x -> x.replaceAll("[^\\w.]", ""))
                .toArray(String[]::new);
        }

        int numberOfArgumentsInInitMethod = initMethodParameters.length - 1;
        if (numberOfArgumentsInInitMethod != argumentsInDIFile.length) {
            String message = "Could not find the injected implementation for \"%s\".\nThe __init__ method has %d arguments, but the DI file has %d injected arguments.".formatted(
                targetClass, numberOfArgumentsInInitMethod, argumentsInDIFile.length);
            PypendencyNotifier.notify(dependencyInjectionFile.getProject(), message, NotificationType.ERROR);
            return null;
        }

        return argumentsInDIFile[targetClassIndexInInit];
    }

    private String getFqnFromIdentifier(PsiManager manager, String targetIdentifier) {
        PsiFile targetDiFile = DependencyInjectionFileResolverByIdentifier.resolve(manager, targetIdentifier);

        if (targetDiFile == null) {
            return null;
        }

        return FqnExtractor.extractFqnFromDIFile(targetDiFile);
    }

    @Override
    protected @NotNull @NlsContexts.HintText String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return NOT_FOUND;
    }
}


