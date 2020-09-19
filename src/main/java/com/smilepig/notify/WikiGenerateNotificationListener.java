package com.smilepig.notify;


import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.smilepig.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.HyperlinkEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhuhe on 2020/9/19
 */
public class WikiGenerateNotificationListener implements NotificationListener {

    private final static Logger logger = LoggerFactory.getLogger(WikiGenerateNotificationListener.class);

    @Override
    public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
        String content = notification.getContent();

        // 按指定模式在字符串查找
        String pattern = "href='(.*?)'>";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(content);
        if (m.find()) {
            String url = m.group(1);
            try {
                CommonUtils.browse(new URI(url));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            logger.debug("get url success,content:{}", content);
        } else {
            logger.debug("NO MATCH,content:{}", content);
        }
    }

    public static void main(String[] args) {
        String content = "<a href='http://www.baidu.com'>link</a>";
        String pattern = "href='(.*?)'>";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(content);
        if (m.find()) {
            System.out.println(m.group(1));
        } else {
            System.out.println("NO MATCH");
        }
    }
}
