package com.smilepig.provider;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.smilepig.icon.SimpleIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Created by zhuhe on 2020/9/16
 */
public class ProtoLineMarkerProvider implements LineMarkerProvider {

    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {

        if (element instanceof PsiMethod && element.getParent() instanceof PsiClass) {
            return new LineMarkerInfo<>(element.getFirstChild(), element.getTextRange(),
                                        SimpleIcons.FILE, Pass.UPDATE_ALL, null, null, GutterIconRenderer.Alignment.CENTER);
        }
        return null;
    }

}
