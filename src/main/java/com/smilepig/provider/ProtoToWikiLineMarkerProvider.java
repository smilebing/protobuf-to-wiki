package com.smilepig.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.smilepig.icon.SimpleIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by zhuhe on 2020/9/16
 */
public class ProtoToWikiLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (element instanceof PsiMethod) {
            if(!((PsiMethod) element).getName().equals("teacherAnswerSummaryInfo")){
                return;
            }
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(SimpleIcons.FILE).
                            setTargets(element).
                            setTooltipText("Navigate to a simple property");
            result.add(builder.createLineMarkerInfo(element));
        }
    }
}
