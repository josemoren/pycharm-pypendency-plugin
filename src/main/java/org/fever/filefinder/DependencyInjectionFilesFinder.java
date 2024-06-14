package org.fever.filefinder;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.fever.GotoPypendencyOrCodeHandler;

import java.util.Collection;

public class DependencyInjectionFilesFinder {
    public static Collection<VirtualFile> find(FileType fileType, GlobalSearchScope scope) {
        return FileTypeIndex.getFiles(fileType, scope).stream()
                .filter(file -> file.getPath().contains(GotoPypendencyOrCodeHandler.DEPENDENCY_INJECTION_FOLDER))
                .toList();
    }
}