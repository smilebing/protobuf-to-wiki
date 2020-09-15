package com.hasaki.bean;

/**
 * @Description
 * @Date 2020/9/2
 * @Author wenfucheng
 */
public class PageEditInfo {
    private String serverContext = "coursesvc";

    private Long pageId;
    //标题
    private String title;
    //业务描述或备注
    private String remark;

    private String host;

    private String url;
    //请求方式
    private String method;
    //请求头
    private String header;

    //对应proto地址title(文件名+类名)
    private ProtoStructureBean requestProto;

    //对应proto地址title(文件名+类名)
    private ProtoStructureBean responseProto;

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ProtoStructureBean getRequestProto() {
        return requestProto;
    }

    public void setRequestProto(ProtoStructureBean requestProto) {
        this.requestProto = requestProto;
    }

    public ProtoStructureBean getResponseProto() {
        return responseProto;
    }

    public void setResponseProto(ProtoStructureBean responseProto) {
        this.responseProto = responseProto;
    }

    public String getServerContext() {
        return serverContext;
    }

    public void setServerContext(String serverContext) {
        this.serverContext = serverContext;
    }
}
