package com.smilepig.bean;

/**
 * Created by zhuhe on 2020/9/15
 */
public class ProtoMethodBean {
    private JavaTypeBean requestInfo;

    private JavaTypeBean responseInfo;

    private String controllerUrl;

    private String methodUrl;

    private String wikiUrl;

    private String requestMethod;

    public JavaTypeBean getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(JavaTypeBean requestInfo) {
        this.requestInfo = requestInfo;
    }

    public JavaTypeBean getResponseInfo() {
        return responseInfo;
    }

    public void setResponseInfo(JavaTypeBean responseInfo) {
        this.responseInfo = responseInfo;
    }

    public String getControllerUrl() {
        return controllerUrl;
    }

    public void setControllerUrl(String controllerUrl) {
        this.controllerUrl = controllerUrl;
    }

    public String getMethodUrl() {
        return methodUrl;
    }

    public void setMethodUrl(String methodUrl) {
        this.methodUrl = methodUrl;
    }

    public String getWikiUrl() {
        return wikiUrl;
    }

    public void setWikiUrl(String wikiUrl) {
        this.wikiUrl = wikiUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
}
