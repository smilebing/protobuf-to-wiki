package com.smilepig.provider;


import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.smilepig.action.ProtobufToWikiAction;
import com.smilepig.icon.SimpleIcons;
import com.smilepig.service.psi.PsiScanService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by zhuhe on 2020/9/19
 */
public class ProtoLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    public RelatedItemLineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return null;
        }

        DefaultActionGroup actionGroup = new DefaultActionGroup();


        PsiModifierList modifierList = ((PsiMethod) element).getModifierList();
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();
            String text = nameReferenceElement.getText();
            if (text == null) {
                continue;
            }
            if (!PsiScanService.MAPPING_PREFIX_LIST.contains(text)) {
                continue;
            }

            ProtobufToWikiAction protobufToWikiAction = new ProtobufToWikiAction();
            actionGroup.add(protobufToWikiAction);
            ProtoHandler protoHandler = new ProtoHandler();
//            RelatedItemLineMarkerInfo relatedItemLineMarkerInfo = new RelatedItemLineMarkerInfo(modifierList,
//                                                                                                modifierList.getTextRange(),
//                                                                                                SimpleIcons.FILE,
//                                                                                                t -> "what",
//                                                                                                protoHandler,
//                                                                                                GutterIconRenderer.Alignment.RIGHT,
//                                                                                                new ArrayList<GotoRelatedItem>());
            int pass = Pass.UPDATE_ALL;

            RelatedItemLineMarkerInfo marker = new RelatedItemLineMarkerInfo(modifierList, modifierList.getTextRange(), SimpleIcons.FILE, pass, t -> "what", protoHandler, GutterIconRenderer.Alignment.CENTER, new ArrayList<GotoRelatedItem>());

            return marker;
        }

        return null;

    }
}
