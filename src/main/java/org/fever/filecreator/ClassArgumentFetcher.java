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
import org.fever.utils.FqnExtractor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassArgumentFetcher {
    private static final int SELF_INDEX_IN_PARAMETER_LIST = 0;

    public static Collection<FQNItem> getFqnOfInitArguments(PyFile sourceCodeFile) {
        List<FQNItem> fqns = new ArrayList<>();

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
                fqns.add(new FQNItem(null, null, parameter.getName()));
                continue;
            }

            Collection<PyClass> allImplementations = PyClassInheritorsSearch.search(parameterClass, false).findAll();
            if (allImplementations.isEmpty()) {
                PsiFile dependencyInjectionFile = GotoPypendencyOrCodeHandler.getPypendencyDefinition(parameterClass.getContainingFile());
                String parameterClassFqn = FqnExtractor.extractFqnFromDIFile(dependencyInjectionFile);
                if (parameterClassFqn != null) {
                    fqns.add(new FQNItem(parameterClassFqn, parameterClass));
                    continue;
                }
                fqns.add(new FQNItem(null, parameterClass));
                continue;
            }

            for (PyClass implementation: allImplementations) {
                PsiFile dependencyInjectionFile = GotoPypendencyOrCodeHandler.getPypendencyDefinition(implementation.getOriginalElement().getContainingFile());
                String implementationFqn = FqnExtractor.extractFqnFromDIFile(dependencyInjectionFile);
                FQNItem item = new FQNItem(implementationFqn, parameterClass);
                if (!fqns.contains(item)) {
                    fqns.add(item);
                }
            }
        }

        return fqns;
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