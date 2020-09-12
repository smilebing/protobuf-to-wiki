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

    private static final String wikiTemplate = "<table><tbody><tr><th><p style=\"text-align: center;\"><span style=\"color: rgb(255,0,0);\">业务描述或备注</span></p></th></tr><tr><td colspan=\"1\"><p>列表接口一定要描述返回数据的内容</p></td></tr></tbody></table><table><tbody><tr><td>HOST</td><td><p><span style=\"color: rgb(255,0,0);\">选择一个</span></p><p><a href=\"http://api.changingedu.com\">http://cdn.</a><a href=\"http://api.changingedu.com\">api.</a><a href=\"http://api.changingedu.com\">changingedu.com</a></p><p><a href=\"http://api.changingedu.com\">http://api.changingedu.com</a></p><p><a href=\"http://api.changingedu.com\">http://api.idc.cedu.cn</a></p></td></tr><tr><td>URL</td><td>/api/...</td></tr><tr><td colspan=\"1\">Description</td><td colspan=\"1\"><span style=\"color: rgb(192,192,192);\">接口主要功能</span></td></tr><tr><td>Method</td><td>POST</td></tr><tr><td>Header</td><td>Content-Type: application/x-protobuf</td></tr><tr><td><span>Request-Body</span></td><td><p><ac:structured-macro ac:name=\"include\"><ac:parameter ac:name=\"\"><ac:link><ri:page ri:content-title=\"utils-SimpleBoolRequest\" /></ac:link></ac:parameter></ac:structured-macro></p></td></tr><tr><td><span>Response</span></td><td><ac:structured-macro ac:name=\"include\"><ac:parameter ac:name=\"\"><ac:link><ri:page ri:content-title=\"resp-SimpleResponse\" /></ac:link></ac:parameter></ac:structured-macro></td></tr><tr><td colspan=\"1\">Error Code</td><td colspan=\"1\">&nbsp;</td></tr></tbody></table>";

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

        //标题
        if(pageEditInfo.getTitle() != null){
            page.setTitle(pageEditInfo.getTitle());
        }
        //内容
        String result = editPageContent(pageEditInfo, page.getContent());
        page.setContent(result);
        confluenceSoapService.storePage(token, page);
    }

    private String editPageContent(PageEditInfo pageEditInfo, String content) {
        Document document = Jsoup.parse(content);

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

        return htmlCompressor.compress(document.outerHtml());
    }


    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("D:\\account.properties"));
        AccountInfo.username = properties.getProperty("username");
        AccountInfo.password = properties.getProperty("password");

        InterfacePageService interfacePageService = getInstance();
        PageEditInfo pageEditInfo = new PageEditInfo();
        pageEditInfo.setPageId(224494261L);
        pageEditInfo.setTitle("测试测试测试666");
        pageEditInfo.setRemark("测试测试666");
        pageEditInfo.setHost("host1111");
        pageEditInfo.setUrl("url111111");
        pageEditInfo.setMethod("gogogo");
        pageEditInfo.setHeader("header111111111");
        pageEditInfo.setRequestBodyTitle("orderservice_activity-OrderServiceActivityParticipateResponse");
        pageEditInfo.setResponseBodyTitle("resp-SimpleResponse");
        interfacePageService.createPageInfo(pageEditInfo,224494261L);
    }

    public void createPageInfo(PageEditInfo pageEditInfo, Long parentId) throws RemoteException {
        RemotePage page = new RemotePage();
        page.setParentId(parentId);
        page.setTitle(pageEditInfo.getTitle());
        String content = editPageContent(pageEditInfo, wikiTemplate);
        page.setContent(content);
        page.setSpace("BS");
        confluenceSoapService.storePage(token, page);
    }


}
