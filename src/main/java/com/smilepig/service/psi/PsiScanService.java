package com.smilepig.service.psi;

import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.tree.java.PsiNameValuePairImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.smilepig.bean.JavaTypeBean;
import com.smilepig.bean.ProtoMethodBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhuhe on 2020/9/19
 */
public class PsiScanService {

    private final static Logger logger = LoggerFactory.getLogger(PsiScanService.class);

    public final static String PROTO_REQUEST_PREFIX = "@ProtoRequestBody";
    public final static List<String> MAPPING_PREFIX_LIST = Arrays.asList("RequestMapping", "GetMapping", "PostMapping");


    public ProtoMethodBean getControllerInfo(Project project, PsiElement element) {

        ProtoMethodBean protoMethodBean = new ProtoMethodBean();

        PsiMethod[] customizes = PsiShortNamesCache.getInstance(project)
                .getMethodsByName("customize", GlobalSearchScope.projectScope(project));

        for (PsiMethod customize : customizes) {
            PsiParameter[] parameters = customize.getParameterList().getParameters();
            if (parameters.length != 1) {
                continue;
            }
            PsiParameter parameter = parameters[0];
            if (!parameter.getType()
                    .getCanonicalText()
                    .equals("org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory")) {
                continue;
            }

            String text = customize.getText();

            // 按指定模式在字符串查找
            String pattern = "setContextPath(.*?);";
            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);

            // 现在创建 matcher 对象
            Matcher m = r.matcher(text);
            if (m.find()) {
                String context = m.group(1);
                context = context.replaceAll("/", "").replaceAll("\"", "").replaceAll("\\(", "").replaceAll("\\)", "");
                protoMethodBean.setApplicationContext(context);
                break;
            }
        }

        if (protoMethodBean.getApplicationContext() == null) {
            return null;
        }


        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (psiClass == null) {
            return null;
        }

        PsiAnnotation[] classAnnotations = psiClass.getAnnotations();
        for (PsiAnnotation classAnnotation : classAnnotations) {

            PsiJavaCodeReferenceElement nameReferenceElement = classAnnotation.getNameReferenceElement();
            String text = nameReferenceElement.getText();
            if (text == null) {
                continue;
            }
            if (!MAPPING_PREFIX_LIST.contains(text)) {
                continue;
            }

            List<JvmAnnotationAttribute> attributes = classAnnotation.getAttributes();
            for (JvmAnnotationAttribute attribute : attributes) {
                PsiAnnotationMemberValue detachedValue = ((PsiNameValuePairImpl) attribute).getDetachedValue();
                if (detachedValue == null) {
                    continue;
                }
                if (attribute.getAttributeName().equals("value")) {
                    protoMethodBean.setControllerUrl(detachedValue.getText().replaceAll("\"", ""));
                }
            }
        }


        if (containingMethod == null) {
            return null;
        }

        PsiDocComment docComment = containingMethod.getDocComment();
        if (docComment != null) {
            protoMethodBean.setHasDoc(true);
            PsiDocTag[] tags = docComment.getTags();
            for (PsiDocTag tag : tags) {
                if (tag.getName().equals("wiki")) {
                    String wikiUrl=tag.getText().replace("*","").replace("\n","").replace("@wiki","");
                    wikiUrl= wikiUrl.trim();
                    //wiki 链接
                    if(StringUtils.isNotEmpty(wikiUrl)){
                        protoMethodBean.setWikiUrl(wikiUrl);
                    }
                }
                if (tag.getName().equals("title")) {
                    //方法名
                    String title=tag.getText().replace("*","").replace("\n","").replace("@title","");
                    title= title.trim();
                    //wiki 链接
                    if(StringUtils.isNotEmpty(title)){
                        protoMethodBean.setWikiTitle(title);
                    }
                }
            }
        } else {
            protoMethodBean.setHasDoc(false);
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

        JavaTypeBean requestTypeJavaTypeBean = null;
        if (requestPsiParameter != null) {
            requestTypeJavaTypeBean = PsiJarService.getJavaTypeBean(project, requestPsiParameter.getTypeElement());
        }


        PsiAnnotation requestMappingAnno = null;

        PsiModifierList modifierList = containingMethod.getModifierList();
        PsiAnnotation[] annotations = modifierList.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();
            String text = nameReferenceElement.getText();
            if (text == null) {
                continue;
            }
            if (MAPPING_PREFIX_LIST.contains(text)) {
                requestMappingAnno = annotation;
                List<JvmAnnotationAttribute> attributes = requestMappingAnno.getAttributes();
                for (JvmAnnotationAttribute attribute : attributes) {
                    PsiAnnotationMemberValue detachedValue = ((PsiNameValuePairImpl) attribute).getDetachedValue();
                    if (detachedValue == null) {
                        continue;
                    }
                    if (attribute.getAttributeName().equals("value")) {
                        protoMethodBean.setMethodUrl(detachedValue.getText().replaceAll("\"", ""));
                    }
                    if (attribute.getAttributeName().equals("method")) {
                        protoMethodBean.setRequestMethod(detachedValue.getText());
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


//    public ProtoMethodBean getControllerInfo(AnActionEvent anActionEvent) {
//        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
//        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
//        Project project = anActionEvent.getProject();
//
//        if (editor == null || psiFile == null) {
//            return null;
//        }
//        int offset = editor.getCaretModel().getOffset();
//
//        PsiElement element = psiFile.findElementAt(offset);
//
//        return getControllerInfo(element);
//    }
}
