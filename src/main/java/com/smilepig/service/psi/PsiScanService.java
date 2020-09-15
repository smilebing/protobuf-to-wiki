package com.smilepig.service.psi;

import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.smilepig.bean.JavaTypeBean;
import com.smilepig.bean.ProtoMethodBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zhuhe on 2020/9/3
 */
public class PsiScanService {

    private final static Logger logger = LoggerFactory.getLogger(PsiScanService.class);

    private final static String PROTO_REQUEST_PREFIX = "@ProtoRequestBody";
    private final static String REQUEST_MAPPING_PREFIX = "RequestMapping";

    public ProtoMethodBean getControllerInfo(AnActionEvent anActionEvent) {
        ProtoMethodBean protoMethodBean = new ProtoMethodBean();

        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();

        PsiElement element = psiFile.findElementAt(offset);

        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

        if (containingMethod == null) {
            return null;
        }

        PsiDocComment docComment = containingMethod.getDocComment();
        if (docComment != null) {
            PsiDocTag[] tags = docComment.getTags();
            for (PsiDocTag tag : tags) {
                if (tag.getName().equals("wiki")) {
                    //wiki 链接
                    if (tag.getValueElement() == null) {
                        return null;
                    }
                    String wikiUrl = tag.getValueElement().getText();
                    logger.info("wiki link:{}", wikiUrl);
                }
            }
        }

        Project project = anActionEvent.getProject();
        if (project == null) {
            return null;
        }
        PsiTypeElement responseTypeElement = containingMethod.getReturnTypeElement();
        if (responseTypeElement == null) {
            return null;
        }

        //返回值
        JavaTypeBean responseJavaTypeBean = PsiJarService.getJavaTypeBean(project, responseTypeElement);
        PsiParameter requestPsiParameter = null;
        PsiParameterList parameterList = containingMethod.getParameterList();
        for (PsiParameter parameter : parameterList.getParameters()) {
            parameter.getType();
            PsiAnnotation[] annotations = parameter.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                if (PROTO_REQUEST_PREFIX.equals(annotation.getText())) {
                    requestPsiParameter = parameter;
                    break;
                }
            }
        }

        if (requestPsiParameter == null) {
            return null;
        }
        JavaTypeBean requestTypeJavaTypeBean = PsiJarService.getJavaTypeBean(project, requestPsiParameter.getTypeElement());


        PsiAnnotation requestMappingAnno = null;

        PsiModifierList modifierList = containingMethod.getModifierList();
        PsiAnnotation[] annotations = modifierList.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();
            String text = nameReferenceElement.getText();
            if (text == null) {
                continue;
            }
            if (REQUEST_MAPPING_PREFIX.equals(text)) {
                requestMappingAnno = annotation;
                List<JvmAnnotationAttribute> attributes = requestMappingAnno.getAttributes();
                for (JvmAnnotationAttribute attribute : attributes) {
                    if (attribute.getAttributeName().equals("value")) {
                        protoMethodBean.setMethodUrl(attribute.getAttributeValue().toString());
                    }
                    if (attribute.getAttributeName().equals("method")) {
                        protoMethodBean.setMethod(attribute.getAttributeValue().toString());
                    }
                }

            }
        }


        if (requestTypeJavaTypeBean != null) {
            protoMethodBean.setRequestInfo(requestTypeJavaTypeBean);
        }

        if (responseJavaTypeBean != null) {
            protoMethodBean.setResponseInfo(responseJavaTypeBean);
        }

        return protoMethodBean;


    }
}
