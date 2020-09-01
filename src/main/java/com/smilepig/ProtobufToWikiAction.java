package com.smilepig;

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
import com.smilepig.notify.LoginDialog;
import com.smilepig.notify.SimpleNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Created by zhuhe on 2020/8/28
 */
public class ProtobufToWikiAction extends AnAction {

    private final static Logger logger = LoggerFactory.getLogger(ProtobufToWikiAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        assert editor != null;
        SelectionModel selectionModel = editor.getSelectionModel();
        logger.info("selectionModel:{}", selectionModel);
        String selectedText = selectionModel.getSelectedText();

        //登陆
        LoginDialog loginDialog = new LoginDialog(true);
        loginDialog.show();
        if (loginDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            String name = loginDialog.getjTextFieldName().getText().trim();
            String pwd = loginDialog.getjTextFieldPwd().getText().trim();
            boolean selected = loginDialog.getjCheckBox().isSelected();
            logger.debug("登陆,name:{},pwd:{},selected:{}", name, pwd, selected);
            //todo:zh 登陆
        } else {
            return;
        }

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                System.out.println("run in other thread");
            }
        });

        Messages.showMessageDialog(project, "登陆成功", "提示", Messages.getInformationIcon());

        JBPopupFactory factory = JBPopupFactory.getInstance();
        BalloonBuilder htmlTextBalloonBuilder = factory.createHtmlTextBalloonBuilder("内容", null, JBColor.PINK, new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                System.out.println("hyper link");
            }
        });

        htmlTextBalloonBuilder.setFadeoutTime(5 * 1000)
                .createBalloon()
                .show(factory.guessBestPopupLocation(editor), Position.below);


        //发送通知
        SimpleNotification.notify(project, "<a href='http://www.baidu.com'>link</a>");
    }
}