package com.smilepig.action;

import com.hasaki.bean.PageEditInfo;
import com.hasaki.page.InterfacePageService;
import com.hasaki.proto.GetProtoBufStructure;
import com.hasaki.wiki.RemotePage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon.Position;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.smilepig.bean.ProtoMethodBean;
import com.smilepig.notify.LoginDialog;
import com.smilepig.notify.SimpleNotification;
import com.smilepig.service.psi.PsiScanService;
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
        assert editor != null;
        SelectionModel selectionModel = editor.getSelectionModel();
        logger.info("selectionModel:{}", selectionModel);
        String selectedText = selectionModel.getSelectedText();


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
        ProtoMethodBean controllerInfo = psiScanService.getControllerInfo(e);

        controllerInfo.setWikiTitle("测试测试测试121321312");
        //生成wiki
        PageEditInfo pageEditInfo=GetProtoBufStructure.getProto(controllerInfo);
        RemotePage remotePage;
        try {
            remotePage = pageService.createPageInfo(pageEditInfo, 134284818L);
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


        //发送通知
        SimpleNotification.notify(project, String.format("<a href='%s'>%s</a>",remotePage.getUrl(), remotePage.getTitle()));
    }
}
