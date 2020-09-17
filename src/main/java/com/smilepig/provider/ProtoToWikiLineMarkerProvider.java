package com.smilepig.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.smilepig.icon.SimpleIcons;
import com.smilepig.service.psi.PsiScanService;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by zhuhe on 2020/9/16
 */
public class ProtoToWikiLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (element instanceof PsiMethod) {
            PsiModifierList modifierList = ((PsiMethod) element).getModifierList();
            for (PsiAnnotation annotation : modifierList.getAnnotations()) {
                PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();
                String text = nameReferenceElement.getText();
                if (text == null) {
                    continue;
                }
                if (!PsiScanService.REQUEST_MAPPING_PREFIX.equals(text)) {
                    continue;
                }

                NavigationGutterIconBuilder<PsiElement> builder =
                        NavigationGutterIconBuilder.create(SimpleIcons.FILE)
                                .setTargets(element)
                                .setTooltipText("生成wiki接口")
                                .setPopupTitle("生成wiki接口");
                result.add(builder.createLineMarkerInfo(element));
            }
        }
    }
}
