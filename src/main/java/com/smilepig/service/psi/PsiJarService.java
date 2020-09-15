package com.smilepig.service.psi;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.PsiUtil;
import com.smilepig.bean.JavaTypeBean;

/**
 * Created by zhuhe on 2020/9/15
 */
public class PsiJarService {
    public static JavaTypeBean getJavaTypeBean(Project project,
                                               PsiTypeElement element) {
        if (project == null) {
            return null;
        }

        if (element == null) {
            return null;
        }

        PsiClass psiClass = PsiUtil.resolveClassInType(element.getType());
        assert psiClass != null;
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile == null) {
            return null;
        }
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        for (OrderEntry orderEntry : fileIndex.getOrderEntriesForFile(virtualFile)) {
            if (orderEntry instanceof LibraryOrderEntry) {
                final LibraryOrderEntry libraryEntry = (LibraryOrderEntry) orderEntry;
                final Library library = libraryEntry.getLibrary();
                if (library == null) {
                    continue;
                }
                VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                if (files.length == 0) {
                    continue;
                }
                for (VirtualFile jar : files) {
                    JavaTypeBean javaTypeBean = new JavaTypeBean();
                    javaTypeBean.setClassType(element.getText());
                    javaTypeBean.setJarPath(jar.getPath());
                    return javaTypeBean;
                }
            }
        }
        return null;
    }
}
