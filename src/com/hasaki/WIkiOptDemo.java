package com.hasaki;

import com.hasaki.wiki.ConfluenceSoapService;
import com.hasaki.wiki.ConfluenceSoapServiceServiceLocator;
import com.hasaki.wiki.RemotePage;
import com.hasaki.wiki.RemoteSearchResult;

import javax.xml.rpc.ServiceException;
import java.io.IOException;

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
        ConfluenceSoapServiceServiceLocator locator = new ConfluenceSoapServiceServiceLocator();
        ConfluenceSoapService confluenceSoapService = locator.getConfluenceserviceV2();
        String token = confluenceSoapService.login("username", "password");

        RemoteSearchResult[] search = confluenceSoapService.search(token, "PBFF 父课程上课接口", 10);
        for (RemoteSearchResult remoteSearchResult : search) {
            long id = remoteSearchResult.getId();
            RemotePage page = confluenceSoapService.getPage(token, id);
            System.out.println(page.getContent());
        }

        //confluenceSoapService.storePage()
        System.out.println(search);
    }

}
