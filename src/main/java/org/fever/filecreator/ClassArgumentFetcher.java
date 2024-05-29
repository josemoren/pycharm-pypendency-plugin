package org.fever.filecreator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import groovyjarjarantlr4.v4.misc.OrderedHashMap;
import org.apache.commons.lang.ArrayUtils;
import org.fever.fileresolver.DependencyInjectionFileResolverByClassName;
import org.fever.utils.ClassArgumentParser;
import org.fever.utils.IdentifierExtractor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ClassArgumentFetcher {
    private static final int SELF_INDEX_IN_PARAMETER_LIST = 0;
    public static Collection<IdentifierItem> getFqnOfInitArguments(PyClass originClass) {
        List<IdentifierItem> identifiers = new ArrayList<>();
        PyFile sourceCodeFile = (PyFile) originClass.getContainingFile();
        Project project = sourceCodeFile.getProject();

        TypeEvalContext context = TypeEvalContext.userInitiated(
                project,
                sourceCodeFile
        );
        Map<PyParameter, @Nullable PyClass> initParamsToClasses = getClassesFromInitParams(originClass, context);

        for (Map.Entry<PyParameter, @Nullable PyClass> entry : initParamsToClasses.entrySet()) {
            PyParameter parameter = entry.getKey();
            PyClass parameterClass = entry.getValue();
            String className = ClassArgumentParser.parse(parameter.getText());
            List<String> implementationsClassNames = getAllImplementationsClassNames(parameterClass, className);
            implementationsClassNames.add(className);

            for (String implementationClassName : implementationsClassNames) {
                PsiFile dependencyInjectionFile = DependencyInjectionFileResolverByClassName.resolve(project, implementationClassName);
                String implementationIdentifier = IdentifierExtractor.extractIdentifierFromDIFile(dependencyInjectionFile);
                IdentifierItem item = new IdentifierItem(implementationIdentifier, parameterClass, parameter);
                if (!identifiers.contains(item)) {
                    identifiers.add(item);
                }
            }
        }

        return identifiers;
    }

    @NotNull
    private static List<String> getAllImplementationsClassNames(@Nullable PyClass parameterClass, String className) {
        List<String> EMPTY_LIST = new ArrayList<>();


        if (parameterClass == null) {
            return EMPTY_LIST;
        }

        boolean classIsBuiltin = Character.isLowerCase(className.charAt(0));
        if (classIsBuiltin) {
            return EMPTY_LIST;
        }

        return PyClassInheritorsSearch
                .search(parameterClass, false)
                .findAll()
                .stream()
                .map(PyClass::getName)
                .collect(Collectors.toList());
    }

    private static Map<PyParameter, @Nullable PyClass> getClassesFromInitParams(PyClass originClass, TypeEvalContext context) {
        PyFunction initMethod = originClass.findInitOrNew(false, context);
        if (initMethod == null) {
            return new HashMap<>();
        }
        PyParameter[] initParameters = initMethod.getParameterList().getParameters();
        PyParameter[] initParametersWithoutSelf = (PyParameter[]) ArrayUtils.remove(initParameters, SELF_INDEX_IN_PARAMETER_LIST);

        Map<PyParameter, @Nullable PyClass> paramsToClasses = new OrderedHashMap<>();
        for (PyParameter parameter : initParametersWithoutSelf) {
            PyType argumentType = parameter.getAsNamed().getArgumentType(context);
            if (!(argumentType instanceof PyClassType)) {
                paramsToClasses.put(parameter, null);
                continue;
            }
            PyClass pyClass = ((PyClassType) argumentType).getPyClass();
            paramsToClasses.put(parameter, pyClass);
        }
        return paramsToClasses;
    }
}
