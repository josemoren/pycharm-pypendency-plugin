package org.fever.filefinder;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.fever.codeInsight.GotoPypendencyOrCodeHandler;
import org.fever.filecreator.DIFileType;

import java.util.Collection;
import java.util.stream.Stream;

public class DependencyInjectionFilesFinder {
    public static Collection<VirtualFile> find(FileType fileType, GlobalSearchScope scope) {
        Stream<VirtualFile> allFiles = FileTypeIndex.getFiles(fileType, scope).stream();

        if (DIFileType.PYTHON.getFileType().equals(fileType)) {
            allFiles = allFiles.filter(
                file -> file.getPath().contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER)
            );
        }

        return allFiles.toList();
    }
}
