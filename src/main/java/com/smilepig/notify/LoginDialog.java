package com.smilepig.notify;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by zhuhe on 2020/9/19
 */
public class LoginDialog extends DialogWrapper {

    private JTextField jTextFieldName;
    private JPasswordField jTextFieldPwd;
    private JCheckBox jCheckBox;

    public LoginDialog(boolean canBeParent) {
        super(canBeParent);
        setTitle("protobuf-to-wiki");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        JPanel jPanel = new JPanel(new FlowLayout());

        jPanel.setSize(400, 200);
        jTextFieldName = new JTextField();
        jTextFieldPwd = new JPasswordField();
        JLabel jLabelName = new JLabel("域账号");
        JLabel jLabelPwd = new JLabel("密码");
        jCheckBox = new JCheckBox("记住密码");

        jPanel.add(jLabelName);
        jPanel.add(jTextFieldName);
        jPanel.add(jLabelPwd);
        jPanel.add(jTextFieldPwd);
        jPanel.add(jCheckBox);

        setOKButtonText("登录");
        setCancelButtonText("取消");

        return jPanel;
    }

    public JTextField getjTextFieldName() {
        return jTextFieldName;
    }

    public void setjTextFieldName(JTextField jTextFieldName) {
        this.jTextFieldName = jTextFieldName;
    }

    public JTextField getjTextFieldPwd() {
        return jTextFieldPwd;
    }

    public void setjTextFieldPwd(JPasswordField jTextFieldPwd) {
        this.jTextFieldPwd = jTextFieldPwd;
    }

    public JCheckBox getjCheckBox() {
        return jCheckBox;
    }

    public void setjCheckBox(JCheckBox jCheckBox) {
        this.jCheckBox = jCheckBox;
    }
}
