package com.smilepig.bean;

/**
 * Created by zhuhe on 2020/9/19
 */
public class JavaTypeBean {
    private String classType;
    private String jarPath;
    private String packageName;
    private String rootClassName;

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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRootClassName() {
        return rootClassName;
    }

    public void setRootClassName(String rootClassName) {
        this.rootClassName = rootClassName;
    }

    @Override
    public String toString() {
        return "JavaTypeBean{" +
                "classType='" + classType + '\'' +
                ", jarPath='" + jarPath + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
