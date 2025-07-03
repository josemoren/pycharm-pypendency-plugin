package org.fever.filecreator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyParameter;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import groovyjarjarantlr4.v4.misc.OrderedHashMap;
import org.fever.fileresolver.DependencyInjectionFileResolverByClassName;
import org.fever.utils.ClassArgumentParser;
import org.fever.utils.IdentifierExtractor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassArgumentFetcher {
    private static final int SELF_INDEX_IN_PARAMETER_LIST = 0;

    public static Collection<IdentifierItem> getFqnOfInitArguments(PyClass originClass) {
        List<IdentifierItem> identifiers = new ArrayList<>();
        PyFile sourceCodeFile = (PyFile) originClass.getContainingFile();
        Project project = sourceCodeFile.getProject();
        TypeEvalContext context = TypeEvalContext.userInitiated(project, sourceCodeFile);
        Map<PyParameter, @Nullable PyClass> initParamsToClasses = getClassesFromInitParams(originClass, context);

        for (Map.Entry<PyParameter, @Nullable PyClass> entry : initParamsToClasses.entrySet()) {
            PyParameter parameter = entry.getKey();
            PyClass parameterClass = entry.getValue();
            List<String> classNames = getInjectedClassNameAndImplementations(parameter, parameterClass);

            for (String className : classNames) {
                IdentifierItem identifier = getIdentifierItem(className, project, parameter, parameterClass);

                if (!identifiers.contains(identifier)) {
                    identifiers.add(identifier);
                }
            }
        }

        return identifiers;
    }

    @NotNull
    private static List<String> getInjectedClassNameAndImplementations(PyParameter parameter, @Nullable PyClass parameterClass) {
        String injectedClassName = ClassArgumentParser.parse(parameter.getText());

        List<String> allClassNames = new ArrayList<>();
        allClassNames.add(injectedClassName);

        if (parameterClass == null) {
            return allClassNames;
        }

        assert injectedClassName != null;
        boolean classIsBuiltin = Character.isLowerCase(injectedClassName.charAt(0));
        if (classIsBuiltin) {
            return allClassNames;
        }

        List<String> implementationClassNames = PyClassInheritorsSearch.search(parameterClass, false)
                                                                       .findAll()
                                                                       .stream()
                                                                       .map(PyClass::getName)
                                                                       .toList();

        allClassNames.addAll(implementationClassNames);
        return allClassNames;
    }

    private static @NotNull IdentifierItem getIdentifierItem(String className, Project project, PyParameter parameter, PyClass parameterClass) {
        PsiFile dependencyInjectionFile = DependencyInjectionFileResolverByClassName.resolve(project, className);
        List<String> identifiersInDIFile = IdentifierExtractor.extractIdentifiersFromDIFile(dependencyInjectionFile);
        String implementationIdentifier = identifiersInDIFile.stream()
                                                             .filter(identifier -> identifier.endsWith("." + className))
                                                             .findFirst()
                                                             .orElse(null);

        return new IdentifierItem(implementationIdentifier, parameterClass, parameter);
    }

    private static Map<PyParameter, @Nullable PyClass> getClassesFromInitParams(PyClass originClass, TypeEvalContext context) {
        PyFunction initMethod = originClass.findInitOrNew(false, context);
        if (initMethod == null) {
            return new HashMap<>();
        }

        List<PyParameter> dunderInitMethodParameters = new ArrayList<>(
                Arrays.asList(initMethod.getParameterList().getParameters())
        );
        dunderInitMethodParameters.remove(SELF_INDEX_IN_PARAMETER_LIST);

        Map<PyParameter, @Nullable PyClass> paramsToClasses = new OrderedHashMap<>();
        for (PyParameter parameter : dunderInitMethodParameters) {
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
