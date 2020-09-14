package com.hasaki.bean;

import java.util.List;

/**
 * @Description
 * @Author wangzhaoyang
 * @Date 2020/9/2 16:42
 **/
public class ProtoStructureBean {
    private String protoFileName;

    private String protoName;

    private String  protoTitle;

    private String  protoStructure;

    private List<String> subProtoTitles;

    private List<ProtoStructureBean> protoStructureBeans;

    public List<ProtoStructureBean> getProtoStructureBeans() {
        return protoStructureBeans;
    }

    public String getProtoFileName() {
        return protoFileName;
    }

    public void setProtoFileName(String protoFileName) {
        this.protoFileName = protoFileName;
    }

    public void setProtoStructureBeans(List<ProtoStructureBean> protoStructureBeans) {
        this.protoStructureBeans = protoStructureBeans;
    }

    public List<String> getSubProtoTitles() {
        return subProtoTitles;
    }

    public void setSubProtoTitles(List<String> subProtoTitles) {
        this.subProtoTitles = subProtoTitles;
    }

    public String getProtoName() {
        return protoName;
    }

    public void setProtoName(String protoName) {
        this.protoName = protoName;
    }

    public String getProtoTitle() {
        return protoTitle;
    }

    public void setProtoTitle(String protoTitle) {
        this.protoTitle = protoTitle;
    }

    public String getProtoStructure() {
        return protoStructure;
    }

    public void setProtoStructure(String protoStructure) {
        this.protoStructure = protoStructure;
    }
}
