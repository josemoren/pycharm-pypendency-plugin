package org.fever;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
//import com.jetbrains.python.PythonFileType;


//Project project = ... // Obtain the project instance
//
//// Define a scope for the project files
//        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
//
//// Get all Python files using FileTypeIndex
//        PsiFile[] pythonFiles = FileTypeIndex.getFiles(PythonFileType.INSTANCE, scope);


public class YamlCompletionContributor extends CompletionContributor {
    private List<String> completions;

    public YamlCompletionContributor() {
    }


    private List<String> findYamlFilesInDependencyInjectionDirs(Path rootPath) throws IOException {
        List<String> yamlFiles = new ArrayList<>();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isYamlFile(file) && isInDependencyInjectionSubDirectory(file)) {
                    yamlFiles.add(file.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return yamlFiles;
    }

    private boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
    }

    private boolean isInDependencyInjectionSubDirectory(Path path) {

        if (path.getParent() == null) {
            return false;
        }
        String parentDirectoryName = path.getParent().getFileName().toString();

        while (!parentDirectoryName.equals("_dependency_injection")) {
            if (path.getParent() == null) {
                return false;
            }
            path = path.getParent();
            parentDirectoryName = path.getParent().getFileName().toString();
        }

        return true;
    }
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        Project project = context.getProject();
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        FileType fileType = fileTypeManager.getFileTypeByFileName("hola.py");


//        // list of python files in the project
//        files = DirectoryUtil.findFilesByMask(
//                project.getBaseDir(),
//                "*.py",
//                -1
//        );

        // get the current file as a virtualFile
//        PsiFile currentFile = context.getFile();
//        PyClassType[] classes = PsiTreeUtil.getChildrenOfType(psiFile, PyClass.class);

//        PsiFile psiFile = PsiManager.getInstance(project).findFile(currentFile.getVirtualFile());
        String currentProjectBasePath = project.getBasePath();
        if (currentProjectBasePath == null) {
            Notifications.Bus.notify(new Notification(
                    "pypendency",
                    "Pypendency",
                    "Current project path not available.",
                    NotificationType.WARNING
            ));
            return;
        }


//            Notifications.Bus.notify(new Notification(
//                    "pypendency",
//                    "Pypendency",
//                    context.getEditor().getDocument().getText(),
//                    NotificationType.WARNING
//            ));
//            return;


        // populate completions with string hello if the character before the caret is a double quote
        int startOffset = context.getStartOffset();

        if (startOffset < 2) {
            this.completions = new ArrayList<>();
            return;
        }

        if (context.getEditor().getDocument().getText().charAt(startOffset - 2) == '"') {
            this.completions = this.generateCompletion(currentProjectBasePath);
        }


    }

    // write a function to generate the completions for all python fqns in the project
    // this function should be called in the beforeCompletion method




    private List<String> generateCompletion(String currentProjectBasePath) {
        try {
//            List<String> yamlFiles = findYamlFilesInDependencyInjectionDirs(Paths.get(Objects.requireNonNull(project.getBasePath())+"/src"));
            List<String> yamlFiles = findYamlFilesInDependencyInjectionDirs(Paths.get(currentProjectBasePath+"/src"));
            return yamlFiles;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        List<String> output = new ArrayList<>(
//            List.of(
//                        "foo",
//                        "bar",
//                        "baz"
//                )
//        );

//        try {
//            String[] cmd = new String[]{
//                    "/bin/sh", "-c", "~/.pypendency/list_container_keys.py " + currentProjectBasePath
//            };
//            Process process = Runtime.getRuntime().exec(cmd);
//
//            if (notifyErrors(process)) return output;
//
//            String s;
//            BufferedReader stdInput = new BufferedReader(
//                    new InputStreamReader(process.getInputStream())
//            );
//            while ((s = stdInput.readLine()) != null) {
//                output.add(s);
//            }
//
//            process.waitFor();
//        } catch (Exception e) {
//            Notifications.Bus.notify(new Notification(
//                    "pypendency",
//                    "Pypendency",
//                    e.toString(),
//                    NotificationType.WARNING
//            ));
//        }

//        return output;
    }

    private boolean notifyErrors(Process process) throws IOException {
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(process.getErrorStream()));

        String s;
        StringBuilder error = new StringBuilder();
        while ((s = stdError.readLine()) != null) {
            error.append(s);
        }
        if (!error.toString().equals("")) {
            Notifications.Bus.notify(new Notification(
                    "pypendency",
                    "Pypendency",
                    error.toString(),
                    NotificationType.WARNING
            ));
            return true;
        }
        return false;
    }

    public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
        for (String c : this.completions) {
            result.addElement(LookupElementBuilder.create(c));
        }

    }
}