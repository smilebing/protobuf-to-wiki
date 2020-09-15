package com.smilepig.bean;

/**
 * Created by zhuhe on 2020/9/15
 */
public class JavaTypeBean {
    private String classType;
    private String jarPath;

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    @Override
    public String toString() {
        return "JavaTypeBean{" +
                "classType='" + classType + '\'' +
                ", jarPath='" + jarPath + '\'' +
                '}';
    }
}
