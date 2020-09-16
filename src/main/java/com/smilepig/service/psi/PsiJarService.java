package com.smilepig.service.psi;

import com.intellij.lang.java.actions.PsiUtilKt;
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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.util.PsiUtilCore;
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
        if (psiClass == null) {
            return null;
        }

        VirtualFile jarFile = PsiUtil.getJarFile(psiClass);
        if (jarFile == null) {
            return null;
        }

        String packageName = PsiUtil.getPackageName(psiClass);
        PsiClass topmostParentOfType = PsiTreeUtil.getTopmostParentOfType(psiClass, PsiClass.class);
        if (topmostParentOfType == null) {
            return null;
        }

        JavaTypeBean javaTypeBean = new JavaTypeBean();
        javaTypeBean.setClassType(element.getText());
        javaTypeBean.setJarPath(jarFile.getPath());
        javaTypeBean.setPackageName(packageName);
        javaTypeBean.setRootClassName(topmostParentOfType.getName());
        return javaTypeBean;

    }
}
