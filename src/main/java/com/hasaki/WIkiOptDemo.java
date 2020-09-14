package com.hasaki;

import com.hasaki.wiki.ConfluenceSoapService;
import com.hasaki.wiki.ConfluenceSoapServiceServiceLocator;
import com.hasaki.wiki.RemotePageSummary;

import javax.xml.rpc.ServiceException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @Description
 * @Date 2020/8/29
 * @Author wenfucheng
 */
public class WIkiOptDemo {

    /**
     * a simple demo
     *
     * @param args
     * @throws IOException
     * @throws ServiceException
     */
    public static void main(String[] args) throws IOException, ServiceException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("D:\\account.properties"));
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        ConfluenceSoapServiceServiceLocator locator = new ConfluenceSoapServiceServiceLocator();
        ConfluenceSoapService confluenceSoapService = locator.getConfluenceserviceV2();
        String token = confluenceSoapService.login(username, password);

//        RemotePage page = confluenceSoapService.getPage(token, 224494261L);
//        String content = page.getContent();
//        Document document = Jsoup.parse(content);
//
//        Elements elements = document.getElementsMatchingOwnText("HOST");
//        Element element = elements.get(0);
//        Elements children = element.parent().parent().children();
//        for (Element elemment : children) {
//            Element currentEle = elemment.child(1);
//            if("HOST".equals(elemment.child(0).text())){
//                currentEle.text("host");
//            }
//            if("URL".equals(elemment.child(0).text())){
//                currentEle.text("url");
//            }
//            if("Method".equals(elemment.child(0).text())){
//                currentEle.text("method");
//            }
//            if("Header".equals(elemment.child(0).text())){
//                currentEle.text("header");
//            }
//        }
//
//        HtmlCompressor htmlCompressor = new HtmlCompressor();
//        String html = htmlCompressor.compress(document.outerHtml());
//        page.setContent(html);

//        RemoteSearchResult[] search = confluenceSoapService.search(token, "PBFF 父课程上课接口", 10);
//        for (RemoteSearchResult remoteSearchResult : search) {
//            long id = remoteSearchResult.getId();
//            RemotePage page1 = confluenceSoapService.getPage(token, id);
//            System.out.println(page1.getContent());
//        }
//        confluenceSoapService.storePage(token, page);
//        RemotePage remotePage = confluenceSoapService.getPage(token, "BS", "proto.coursesvc");
        RemotePageSummary[] children = confluenceSoapService.getChildren(token, 134284498);
        System.out.println(children);

//        System.out.println(remotePage.getContent());

//        RemotePage storePage = new RemotePage();
//        storePage.setSpace("BS");
//        storePage.setTitle("proto.coursesvc_tst");
//        storePage.setParentId(remotePage.getId());
//        storePage = confluenceSoapService.storePage(token,storePage);
//        System.out.println(storePage.getUrl());

    }
}
