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
import com.smilepig.icon.SimpleIcons;
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
            if (StringUtils.isNotEmpty(wikiUrl)) {
                controllerInfo.setWikiUrl(wikiUrl.trim());
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


//        ApplicationManager.getApplication().runReadAction(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("run in other thread");
//            }
//        });


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
        String title = "@title " + controllerInfo.getWikiTitle();
        String url = "@wiki " + remotePage.getUrl();

        if (docComment != null) {
            PsiDocTag wikiDoc = docComment.findTagByName("wiki");
            PsiDocTag nameDoc = docComment.findTagByName("title");

            if (nameDoc != null) {
                Document document = editor.getDocument();
                if (nameDoc.getValueElement() == null) {
                    TextRange textRange = nameDoc.getNameElement().getTextRange();
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), title)
                    );
                }
            }else{
                PsiDocTag docTagFromText = PsiElementFactory.getInstance(project)
                        .createDocTagFromText(title);
                docComment.add(docTagFromText);
            }

            if (wikiDoc != null) {
                //wiki 链接
                Document document = editor.getDocument();
                if (wikiDoc.getValueElement() == null) {
                    TextRange textRange = wikiDoc.getNameElement().getTextRange();
                    WriteCommandAction.runWriteCommandAction(project, () ->
                            document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), url)
                    );
                }
            }else{
                PsiDocTag docTagFromText = PsiElementFactory.getInstance(project)
                        .createDocTagFromText(url);
                docComment.add(docTagFromText);
            }


        } else {
            PsiDocComment docCommentFromText = PsiElementFactory
                    .getInstance(project).createDocCommentFromText("    /**\n" +
                                                                           "     * " + title + "\n" +
                                                                           "     * " + url + "\n" +
                                                                           "     */\n", containingMethod);

            containingMethod.addBefore(docCommentFromText,containingMethod.getModifierList());
            CodeStyleManager.getInstance (element.getManager ()).reformat (containingMethod);
        }



        //发送通知
        SimpleNotification.notify(project, String.format("<a href='%s'>%s</a>",remotePage.getUrl(), remotePage.getTitle()));
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
