package com.hasaki.page;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.hasaki.bean.PageEditInfo;
import com.hasaki.common.AccountInfo;
import com.hasaki.wiki.ConfluenceSoapService;
import com.hasaki.wiki.ConfluenceSoapServiceServiceLocator;
import com.hasaki.wiki.RemotePage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.rpc.ServiceException;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * @Description
 * @Date 2020/8/30
 * @Author wenfucheng
 */
public class InterfacePageService {

    private static ConfluenceSoapService confluenceSoapService;
    private static String token;
    private HtmlCompressor htmlCompressor = new HtmlCompressor();
    private static InterfacePageService singleton;

    private InterfacePageService(String username, String password) throws ServiceException, RemoteException {
        init(username,password);
    }

    private static synchronized void init(String username, String password) throws ServiceException, RemoteException {
        if(token != null){
            return;
        }
        ConfluenceSoapServiceServiceLocator locator = new ConfluenceSoapServiceServiceLocator();
        confluenceSoapService = locator.getConfluenceserviceV2();
        token = confluenceSoapService.login(username, password);
    }

    public static InterfacePageService getInstance() throws ServiceException, RemoteException {
        if(singleton != null){
            return singleton;
        }
        singleton = new InterfacePageService(AccountInfo.username, AccountInfo.password);
        return singleton;
    }




    /**
     * 更新接口信息
     * @param pageEditInfo
     * @return
     */
    public void updatePageInfo(PageEditInfo pageEditInfo) throws RemoteException {
        RemotePage page = confluenceSoapService.getPage(token, pageEditInfo.getPageId());

        String content = page.getContent();
        Document document = Jsoup.parse(content);
        //标题
        if(pageEditInfo.getTitle() != null){
            page.setTitle(pageEditInfo.getTitle());
        }

        //获取所有include元素 Request-Body Response
        Elements elementsByTag = document.getElementsByTag("ri:page");
        for (Element element : elementsByTag) {
            //向上找五层, Request-Body or Response
            Element parent = element.parent().parent().parent().parent().parent();
            if("Request-Body".equals(parent.text())){
                //todo request对应的proto没有还需要先创建
                if(pageEditInfo.getRequestBodyTitle() != null){
                    element.attr("ri:content-title",pageEditInfo.getRequestBodyTitle());
                }
            }else if("Response".equals(parent.text())){
                //todo response对应的proto没有还需要先创建
                if(pageEditInfo.getResponseBodyTitle() != null){
                    element.attr("ri:content-title",pageEditInfo.getResponseBodyTitle());
                }
            }
        }
        //业务描述或备注
        if(pageEditInfo.getRemark() != null){
            Elements elements = document.getElementsMatchingOwnText("业务描述或备注");
            Element element = elements.get(0);
            element.parent().parent().parent().parent().child(1).child(0).child(0).text(pageEditInfo.getRemark());
        }
        //Host URL Method Header
        Elements elements = document.getElementsMatchingOwnText("HOST");
        Element element = elements.get(0);
        Elements children = element.parent().parent().children();
        for (Element elemment : children) {
            Element currentEle = elemment.child(1);
            if("HOST".equals(elemment.child(0).text()) && pageEditInfo.getHost() != null){
                currentEle.text(pageEditInfo.getHost());
            }
            if("URL".equals(elemment.child(0).text()) && pageEditInfo.getUrl() != null){
                currentEle.text(pageEditInfo.getUrl());
            }
            if("Method".equals(elemment.child(0).text()) && pageEditInfo.getMethod() != null){
                currentEle.text(pageEditInfo.getMethod());
            }
            if("Header".equals(elemment.child(0).text()) && pageEditInfo.getHeader() != null){
                currentEle.text(pageEditInfo.getHeader() );
            }
        }

        String result = htmlCompressor.compress(document.outerHtml());
        page.setContent(result);
        confluenceSoapService.storePage(token, page);
    }


    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("D:\\account.properties"));
        AccountInfo.username = properties.getProperty("username");
        AccountInfo.password = properties.getProperty("password");

        InterfacePageService interfacePageService = getInstance();
        PageEditInfo pageEditInfo = new PageEditInfo();
        pageEditInfo.setPageId(224494261L);
        pageEditInfo.setTitle("测试测试测试");
        pageEditInfo.setRemark("测试测试666");
        pageEditInfo.setHost("host1111");
        pageEditInfo.setUrl("url111111");
        pageEditInfo.setMethod("method11111111");
        pageEditInfo.setHeader("header111111111");
        pageEditInfo.setRequestBodyTitle("coursesvc_third_party_refund-CourseSvcOrderCourseFinishMockRequest");
        pageEditInfo.setResponseBodyTitle("resp-SimpleResponse");
        interfacePageService.updatePageInfo(pageEditInfo);

    }


}
