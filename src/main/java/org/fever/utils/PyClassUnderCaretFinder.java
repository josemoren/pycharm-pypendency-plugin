package org.fever.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;

public class PyClassUnderCaretFinder {
    public static PyClass find(Editor editor, PsiFile file) {
        PsiElement elementUnderCaret = file.findElementAt(editor.getCaretModel().getOffset());
        PyClass pyClassUnderCaret = PsiTreeUtil.getParentOfType(elementUnderCaret, PyClass.class);

        if (pyClassUnderCaret == null) {
            return ((PyFile) file).getTopLevelClasses().get(0);
        }

        return pyClassUnderCaret;
    }
}
