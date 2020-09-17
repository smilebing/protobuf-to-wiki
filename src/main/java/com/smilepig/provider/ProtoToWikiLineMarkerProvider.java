package com.smilepig.provider;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerInfo.LineMarkerGutterIconRenderer;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment;
import com.intellij.openapi.editor.markup.MarkupEditorFilter;
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.smilepig.icon.SimpleIcons;
import com.smilepig.service.psi.PsiScanService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AbstractDocument.LeafElement;
import java.util.Collection;

/**
 * Created by zhuhe on 2020/9/16
 */
public class ProtoToWikiLineMarkerProvider extends RelatedItemLineMarkerProvider {
//extends RelatedItemLineMarkerProvider
//    @Override
//    protected void collectNavigationMarkers(@NotNull PsiElement element,
//                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
//        if (element instanceof PsiMethod) {
//            PsiModifierList modifierList = ((PsiMethod) element).getModifierList();
//            for (PsiAnnotation annotation : modifierList.getAnnotations()) {
//                PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();
//                String text = nameReferenceElement.getText();
//                if (text == null) {
//                    continue;
//                }
//                if (!PsiScanService.REQUEST_MAPPING_PREFIX.equals(text)) {
//                    continue;
//                }
//
//                PsiIdentifier childOfType = PsiTreeUtil.getChildOfType(element, PsiIdentifier.class);
//                NavigationGutterIconBuilder<PsiElement> builder
//                        = NavigationGutterIconBuilder.create(SimpleIcons.FILE);
//                builder.setAlignment(Alignment.CENTER);
//                builder.setTargets(childOfType);
//                builder.setTooltipText("生成wiki接口");
//                builder.setPopupTitle("生成wiki接口");
//
//                DefaultActionGroup actionGroup = new DefaultActionGroup();
//
//                RunLineMarkerInfo runLineMarkerInfo = new RunLineMarkerInfo(element, SimpleIcons.FILE, t -> "what", actionGroup);
//
//                result.add(runLineMarkerInfo);
//            }
//        }
//    }
//
//    static class RunLineMarkerInfo extends LineMarkerInfo<PsiElement> {
//        private final DefaultActionGroup myActionGroup;
//
//        RunLineMarkerInfo(PsiElement element, Icon icon, Function<PsiElement, String> tooltipProvider,
//                          DefaultActionGroup actionGroup) {
//            super(element, element.getTextRange(), icon, 0, tooltipProvider, null, GutterIconRenderer.Alignment.RIGHT);
//            myActionGroup = actionGroup;
//        }
//
//        @Override
//        public GutterIconRenderer createGutterRenderer() {
//            return new LineMarkerGutterIconRenderer<PsiElement>(this) {
//                @Override
//                public AnAction getClickAction() {
//                    return null;
//                }
//
//                @Override
//                public boolean isNavigateAction() {
//                    return true;
//                }
//
//                @Override
//                public ActionGroup getPopupMenuActions() {
//                    return myActionGroup;
//                }
//            };
//        }
//
//        @NotNull
//        @Override
//        public MarkupEditorFilter getEditorFilter() {
//            return MarkupEditorFilterFactory.createIsNotDiffFilter();
//        }
//
//    }
}
