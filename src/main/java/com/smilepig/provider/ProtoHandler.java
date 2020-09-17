package com.smilepig.provider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.psi.PsiElement;
import com.smilepig.action.ProtobufToWikiAction;

import java.awt.event.MouseEvent;

/**
 * Created by zhuhe on 2020/9/17
 */
public class ProtoHandler implements GutterIconNavigationHandler {
    @Override
    public void navigate(MouseEvent mouseEvent, PsiElement element) {
        ProtobufToWikiAction protobufToWikiAction = new ProtobufToWikiAction();
        protobufToWikiAction.actionFromIconClick(element);
    }
}
