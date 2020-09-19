package com.hasaki.bean;

import java.util.List;

/**
 * @Description
 * @Author wangzhaoyang
 * @Date 2020/9/19 10:42
 **/
public class ProtoStructureBean {
    /**
     * 所属文件名
     */
    private String protoFileName;
    /**
     * proto名称
     */
    private String protoName;
    /**
     * 所属文件名+ proto名称
     */
    private String  protoTitle;
    /**
     * proto结构
     */
    private String  protoStructure;
    /**
     * proto包含的子名称
     */
    private List<String> subProtoTitles;
    /**
     * proto导入的架包名称
     */
    private List<String>  importTitleList;

    public List<String> getImportTitleList() {
        return importTitleList;
    }

    public void setImportTitleList(List<String> importTitleList) {
        this.importTitleList = importTitleList;
    }
    /**
     * proto包含的子proto结构
     */
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

    @Override
    public int hashCode() {
        return protoTitle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProtoStructureBean other = (ProtoStructureBean) obj;
        return other.protoTitle.equals(this.protoTitle);
    }

}
