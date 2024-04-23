package org.fever.filecreator;

import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import groovyjarjarantlr4.v4.misc.OrderedHashMap;
import org.apache.commons.lang.ArrayUtils;
import org.fever.GotoPypendencyOrCodeHandler;
import org.fever.utils.IdentifierExtractor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassArgumentFetcher {
    private static final int SELF_INDEX_IN_PARAMETER_LIST = 0;

    public static Collection<IdentifierItem> getFqnOfInitArguments(PyFile sourceCodeFile) {
        List<IdentifierItem> identifiers = new ArrayList<>();

        PyClass pyClass = sourceCodeFile.getTopLevelClasses().get(0);
        TypeEvalContext context = TypeEvalContext.codeCompletion(
                sourceCodeFile.getProject(),
                sourceCodeFile.getContainingFile()
        );
        Map<PyParameter, @Nullable PyClass> initParamsToClasses = getClassesFromInitParams(pyClass, context);

        for (Map.Entry<PyParameter, @Nullable PyClass> entry: initParamsToClasses.entrySet()) {
            PyParameter parameter = entry.getKey();
            PyClass parameterClass = entry.getValue();

            if (parameterClass == null) {
                identifiers.add(new IdentifierItem(null, null, parameter));
                continue;
            }

            Collection<PyClass> allImplementations = PyClassInheritorsSearch.search(parameterClass, false).findAll();
            if (allImplementations.isEmpty()) {
                PsiFile dependencyInjectionFile = GotoPypendencyOrCodeHandler.getPypendencyDefinition(parameterClass.getContainingFile());
                String parameterClassIdentifier = IdentifierExtractor.extractIdentifierFromDIFile(dependencyInjectionFile);
                if (parameterClassIdentifier != null) {
                    identifiers.add(new IdentifierItem(parameterClassIdentifier, parameterClass, parameter));
                    continue;
                }
                identifiers.add(new IdentifierItem(null, parameterClass, parameter));
                continue;
            }

            for (PyClass implementation: allImplementations) {
                PsiFile dependencyInjectionFile = GotoPypendencyOrCodeHandler.getPypendencyDefinition(implementation.getOriginalElement().getContainingFile());
                String implementationIdentifier = IdentifierExtractor.extractIdentifierFromDIFile(dependencyInjectionFile);
                IdentifierItem item = new IdentifierItem(implementationIdentifier, parameterClass, parameter);
                if (!identifiers.contains(item)) {
                    identifiers.add(item);
                }
            }
        }

        return identifiers;
    }

    private static Map<PyParameter, @Nullable PyClass> getClassesFromInitParams(PyClass pyClass, TypeEvalContext context) {
        PyFunction initMethod = pyClass.findInitOrNew(false, context);
        if (initMethod == null) {
            return new HashMap<>();
        }
        PyParameter[] initParameters = initMethod.getParameterList().getParameters();
        PyParameter[] initParametersWithoutSelf = (PyParameter[]) ArrayUtils.remove(initParameters, SELF_INDEX_IN_PARAMETER_LIST);

        Map<PyParameter, @Nullable PyClass> paramsToClasses = new OrderedHashMap<>();
        for (PyParameter parameter: initParametersWithoutSelf) {
            PyType argumentType = parameter.getAsNamed().getArgumentType(context);
            if (!(argumentType instanceof PyClassType)) {
                paramsToClasses.put(parameter, null);
                continue;
            }
            pyClass = ((PyClassType) argumentType).getPyClass();
            paramsToClasses.put(parameter, pyClass);
        }
        return paramsToClasses;
    }
}
