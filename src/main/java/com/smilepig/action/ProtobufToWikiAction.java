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
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import com.smilepig.bean.ProtoMethodBean;
import com.smilepig.notify.LoginDialog;
import com.smilepig.notify.SimpleNotification;
import com.smilepig.service.psi.PsiScanService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.rpc.ServiceException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Created by zhuhe on 2020/9/19
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

        //如果本地有用户名密码, 帮用户登录, 下面流程跳过
        try {
            adaptLoginFromLocal();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("本地登录失败");
        }

        while (pageService == null){
            //登录
            LoginDialog loginDialog = new LoginDialog(true);
            loginDialog.show();
            if (loginDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                String username = loginDialog.getjTextFieldName().getText().trim();
                String password = loginDialog.getjTextFieldPwd().getText().trim();
                boolean selected = loginDialog.getjCheckBox().isSelected();
                logger.debug("登录,username:{},password:{},selected:{}", username, password, selected);

                try {
                    pageService = InterfacePageService.getInstance(username, password);
                    if(selected){
                        //保存用户名密码到本地
                        saveAccountInfo(username, password);
                    }
                    Messages.showMessageDialog(project, "登录成功", "提示", Messages.getInformationIcon());
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.err.println("保存用户名密码失败");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog(project, "用户名或密码错误", "提示", Messages.getInformationIcon());
                }
            }
            if(loginDialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE){
                return;
            }
        }

        //搜索proto相关注解，url
        PsiScanService psiScanService = new PsiScanService();
        ProtoMethodBean controllerInfo = psiScanService.getControllerInfo(project, element);

        while (controllerInfo.getWikiTitle() == null) {
            String wikiTitle = Messages.showInputDialog(project, "请输入接口名称", "提示", Messages.getInformationIcon());
            if(wikiTitle == null){
                return;
            }
            wikiTitle = wikiTitle.trim();
            if (StringUtils.isEmpty(wikiTitle)) {
                Messages.showMessageDialog(project, "请输入有效的名称", "提示", Messages.getWarningIcon());
            }else{
                controllerInfo.setWikiTitle(wikiTitle);
            }
        }

        String parentUrl = null;
        while (parentUrl == null) {
            String inputUrl = Messages.showInputDialog(project, "请输入该接口wiki挂靠的父目录", "提示", Messages.getInformationIcon());
            if(inputUrl == null){
                return;
            }
            inputUrl = inputUrl.trim();
            if (StringUtils.isEmpty(inputUrl) && !inputUrl.startsWith("https://wiki.changingedu.com/pages/viewpage.action?pageId=")) { //todo 正则校验
                Messages.showMessageDialog(project, "请输入有效的wiki路径", "提示", Messages.getWarningIcon());
            }else{
                parentUrl = inputUrl;
            }
        }

        //todo 来个loading框

        String url = "http://www.baidu.com";
        String wikiName = "PT 接口名称";
        //生成wiki
        PageEditInfo pageEditInfo=GetProtoBufStructure.getProto(controllerInfo);
        RemotePage remotePage;
        try {
            if(pageEditInfo.getPageId() != null){
                remotePage = pageService.updatePageInfo(pageEditInfo);
            }else{
                String keyword = "pageId=";
                String parentId = parentUrl.substring(parentUrl.indexOf(keyword) + keyword.length());
                remotePage = pageService.createPageInfo(pageEditInfo, Long.parseLong(parentId));
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
            //生成wiki失败
            throw new RuntimeException();
        }

//        ApplicationManager.getApplication().runReadAction(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("run in other thread");
//            }
//        });


        //弹窗通知wiki生成成功
        JBPopupFactory factory = JBPopupFactory.getInstance();
        BalloonBuilder htmlTextBalloonBuilder = factory.createHtmlTextBalloonBuilder(url, null, JBColor.PINK, new HyperlinkListener() {
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

        if (docComment != null) {
            PsiDocTag wikiDoc = docComment.findTagByName("wiki");
            String wikiDocUrl = "@wiki " + url;
            String title = "@title " + wikiName;

            if (wikiDoc != null) {
                //wiki 链接
                wikiDoc = docComment.findTagByName("wiki");
                updateDoc(project, editor, wikiDocUrl, wikiDoc);
            } else {

                PsiDocTag docTagFromText = PsiElementFactory.getInstance(project)
                        .createDocTagFromText(wikiDocUrl);
                docComment.add(docTagFromText);
            }

            PsiDocTag titleDoc = containingMethod.getDocComment().findTagByName("title");
            if (titleDoc != null) {
                updateDoc(project, editor, title, titleDoc);
            } else {
                PsiDocTag docTagFromText = PsiElementFactory.getInstance(project)
                        .createDocTagFromText(title);
                docComment.add(docTagFromText);
            }

        } else {
            PsiDocComment docCommentFromText = PsiElementFactory
                    .getInstance(project).createDocCommentFromText("    /**\n" +
                                                                           "     * " + wikiName + "\n" +
                                                                           "     * " + url + "\n" +
                                                                           "     */\n", containingMethod);

            containingMethod.addBefore(docCommentFromText,containingMethod.getModifierList());
        }
        CodeStyleManager.getInstance (element.getManager ()).reformat (containingMethod);


        //todo loading框消失

        //发送通知
        SimpleNotification.notify(project, String.format("<a href='%s'>%s</a>", url, wikiName));
    }

    private void updateDocDocument(PsiElement element,
                                   Project project,
                                   Editor editor,
                                   PsiDocComment docComment,
                                   String value, PsiDocTag docTag) {
        if (docTag != null) {
            updateDoc(project, editor, value, docTag);
        } else {
            PsiDocTag docTagFromText = PsiElementFactory.getInstance(project)
                    .createDocTagFromText(value);
            docComment.add (docTagFromText);
            CodeStyleManager.getInstance(element.getManager()).reformat(docComment);
        }
    }

    private void updateDoc(Project project, Editor editor, String url, PsiDocTag wikiDoc) {
        Document document = editor.getDocument();
        if (wikiDoc.getValueElement() == null) {
            TextRange textRange = wikiDoc.getNameElement().getTextRange();
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), url)
            );
        }
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    private void adaptLoginFromLocal() throws IOException, ServiceException {
        String usrHome = System.getProperty("user.home");
        File file =new File(usrHome + "\\account.properties");
        if(file.exists()){
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            pageService = InterfacePageService.getInstance(username, password);
        }
    }

    private void saveAccountInfo(String username, String password) throws IOException {
        String usrHome = System.getProperty("user.home");
        File file =new File(usrHome + "\\account.properties");
        if(!file.exists()){
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("username="+username);
        writer.newLine();
        writer.write("password="+password);
        writer.close();
    }
}
