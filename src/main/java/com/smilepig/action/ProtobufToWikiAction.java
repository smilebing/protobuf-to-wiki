package com.smilepig.action;

import com.hasaki.bean.PageEditInfo;
import com.hasaki.page.InterfacePageService;
import com.hasaki.proto.GetProtoBufStructure;
import com.hasaki.wiki.RemotePage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon.Position;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import com.smilepig.bean.ProtoMethodBean;
import com.smilepig.icon.SimpleIcons;
import com.smilepig.notify.LoginDialog;
import com.smilepig.notify.SimpleNotification;
import com.smilepig.service.psi.PsiScanService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.rmi.RemoteException;

/**
 * Created by zhuhe on 2020/8/28
 */
public class ProtobufToWikiAction extends AnAction {

    private final static Logger logger = LoggerFactory.getLogger(ProtobufToWikiAction.class);

    private InterfacePageService pageService;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        actionFromIconClick(element);
    }

    public void actionFromIconClick(PsiElement element) {
        Project project = element.getProject();
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        assert editor != null;

        // Ensure this isn't part of testing
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        //todo 如果本地有用户名密码, 帮用户登录, 下面流程跳过
        while (pageService == null){
            //登录
            LoginDialog loginDialog = new LoginDialog(true);
            loginDialog.show();
            if (loginDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                String name = loginDialog.getjTextFieldName().getText().trim();
                String pwd = loginDialog.getjTextFieldPwd().getText().trim();
                boolean selected = loginDialog.getjCheckBox().isSelected();
                logger.debug("登录,name:{},pwd:{},selected:{}", name, pwd, selected);

                try {
                    pageService = InterfacePageService.getInstance(name, pwd);
                    if(selected){
                        //todo 保存用户名密码到本地
                    }
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog(project, "用户名或密码错误", "提示", Messages.getInformationIcon());
                }
            }
            if(loginDialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE){
                return;
            }
        }

        Messages.showMessageDialog(project, "登录成功", "提示", Messages.getInformationIcon());


        //搜索proto相关注解，url
        PsiScanService psiScanService = new PsiScanService();
        ProtoMethodBean controllerInfo = psiScanService.getControllerInfo(project, element);
        if (controllerInfo.getWikiTitle() == null) {
            String wikiTitle = Messages.showInputDialog(project, "请输入接口名称", "提示", SimpleIcons.FILE);
            wikiTitle = wikiTitle.trim();
            if (StringUtils.isEmpty(wikiTitle)) {
                Messages.showMessageDialog(project, "请输入有效的名称", "提示", Messages.getInformationIcon());
                return;
            }
            controllerInfo.setWikiTitle(wikiTitle);
        }

        if (controllerInfo.getWikiUrl() == null) {
            String wikiUrl = Messages.showInputDialog(project, "请输入wiki地址,新接口不用输入", "提示", SimpleIcons.FILE);
            wikiUrl = wikiUrl.trim();
            if (StringUtils.isEmpty(wikiUrl)) {
                controllerInfo.setWikiUrl(wikiUrl);
            }
        }



        //生成wiki
        PageEditInfo pageEditInfo=GetProtoBufStructure.getProto(controllerInfo);
        RemotePage remotePage;
        try {
            if(pageEditInfo.getPageId() != null){
                remotePage = pageService.updatePageInfo(pageEditInfo);
            }else{
                remotePage = pageService.createPageInfo(pageEditInfo, 134284818L);
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
            //生成wiki失败
            throw new RuntimeException();
        }


        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                System.out.println("run in other thread");
            }
        });


        //弹窗通知wiki生成成功
        JBPopupFactory factory = JBPopupFactory.getInstance();
        BalloonBuilder htmlTextBalloonBuilder = factory.createHtmlTextBalloonBuilder(remotePage.getUrl(), null, JBColor.PINK, new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                System.out.println("hyper link");
            }
        });

        htmlTextBalloonBuilder.setFadeoutTime(5 * 1000)
                .createBalloon()
                .show(factory.guessBestPopupLocation(editor), Position.below);


        //填充注释
        PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiDocComment docComment = containingMethod.getDocComment();
        boolean hasWikiDoc = false;
        boolean hasNameDoc = false;
        if (docComment != null) {
            PsiDocTag wikiDoc = docComment.findTagByName("wiki");
            PsiDocTag nameDoc = docComment.findTagByName("name");

            if (wikiDoc != null) {
                //wiki 链接
                hasWikiDoc = true;
                Document document = editor.getDocument();
                if (wikiDoc.getValueElement() == null) {
                    //有@wiki，没有wiki链接
                    String url = "@wiki wiki.changingedu.com/lalala";
                    TextRange textRange = wikiDoc.getNameElement().getTextRange();
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), url)
                    );
                }
            }

            if (nameDoc != null) {
                hasNameDoc = true;
                Document document = editor.getDocument();
                if (nameDoc.getValueElement() == null) {
                    String url = "@name " + controllerInfo.getWikiTitle();
                    TextRange textRange = nameDoc.getNameElement().getTextRange();
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), url)
                    );
                }
            }

        } else {
            PsiDocComment docCommentFromText = PsiElementFactory
                    .getInstance(project).createDocCommentFromText("    /**\n" +
                                                  "     * 学生答疑欠费详情\n" +
                                                  "     *\n" +
                                                  "     * @param httpServletRequest\n" +
                                                  "     * @return\n" +
                                                  "     */\n",containingMethod);

            containingMethod.addBefore(docCommentFromText,containingMethod.getModifierList());
            CodeStyleManager.getInstance (element.getManager ()).reformat (containingMethod);
        }



        //发送通知
        SimpleNotification.notify(project, String.format("<a href='%s'>%s</a>",remotePage.getUrl(), remotePage.getTitle()));
    }
}
