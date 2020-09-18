package com.hasaki.proto;


import com.google.gson.Gson;
import com.hasaki.bean.PageEditInfo;
import com.hasaki.bean.ProtoStructureBean;
import com.smilepig.bean.JavaTypeBean;
import com.smilepig.bean.ProtoMethodBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Description
 * @Author wangzhaoyang
 * @Date 2020/8/29 13:01
 **/
public class GetProtoBufStructure {
    /**
     * 获取proto结构关系
     *
     * @param jarFiles
     * @param protoNameList
     * @param protoStructureBeans
     * @throws Exception
     */
    public static void getJarName(Set<String> jarFiles, Map<String, String> protoNameList, List<ProtoStructureBean> protoStructureBeans) throws Exception {

        for (String jarFile : jarFiles) {
            try {
                JarFile jar = new JarFile(jarFile);
                //返回zip文件条目的枚举
                Enumeration<JarEntry> enumFiles = jar.entries();
                JarEntry entry;
                //测试此枚举是否包含更多的元素
                while (enumFiles.hasMoreElements() && protoNameList.size() > 0) {
                    entry = enumFiles.nextElement();
                    //只获取proto后缀的文件
                    if (!entry.getName().contains("META-INF") || !entry.getName().contains("com")) {
                        String classFullName = entry.getName();
                        if (classFullName.endsWith(".proto")) {
                            //文件名称
                            String fileName = classFullName.substring(0, classFullName.indexOf(".proto"));
                            //获取文件流
                            InputStream input = jar.getInputStream(entry);
                            InputStreamReader isr = new InputStreamReader(input);
                            BufferedReader reader = new BufferedReader(isr);
                            //
                            String line;
                            //对应java类的名称
                            String classname = "";
                            //是否匹配到方法名
                            boolean isMatchMethod = false;
                            List<String> stringLine = new ArrayList<>();
                            StringBuilder stringBuilder = new StringBuilder();
                            //括号匹配原则，当左右括号相等时，停止读取
                            int bracketsIndex = 0;
                            //记录子方法名
                            List<String> sonProto = new ArrayList<>();
                            //记录此文件导入的所有包
                            List<String> importList = new ArrayList<>();
                            importList.add(fileName);
                            boolean isOneOf = false;
                            while ((line = reader.readLine()) != null) {
                                if (line.matches("\\s*option\\s*java_outer_classname\\s+.*")) {
                                    classname = line;
                                }
                                if (line.matches("\\s*import\\s+.*")) {
                                    importList.add(line.substring(line.lastIndexOf("/") + 1, line.indexOf(".proto")));
                                }
                                //上一行匹配到方法后，往下开始写入结构
                                if (isMatchMethod) {
                                    if (line.contains("}")) {
                                        bracketsIndex--;
                                        isOneOf = false;
                                    }
                                    if (line.contains("{")) {
                                        bracketsIndex++;
                                    }
                                    stringBuilder.append(line + "\n");
                                    sonProto(line, sonProto, isOneOf);
                                    if (line.matches("\\s*oneof\\s+.*")) {
                                        isOneOf = true;
                                    }
                                    if (bracketsIndex == 0) {
                                        ProtoStructureBean protoStructureBean = protoStructureBeans.get(protoStructureBeans.size() - 1);
                                        protoStructureBean.setProtoStructure(stringBuilder.toString());
                                        if (sonProto.size() > 0) {
                                            protoStructureBean.setSubProtoTitles(sonProto);
                                            for (String s : sonProto) {
                                                protoNameList.put(s + "-" + protoStructureBean.getProtoName(), protoStructureBean.getProtoTitle());
                                            }
                                        }
                                        sonProto = new ArrayList<>();
                                        stringBuilder = new StringBuilder();
                                        isMatchMethod = false;
                                    }
                                }
                                Iterator<String> it = protoNameList.keySet().iterator();
                                while (it.hasNext()) {
                                    String s = it.next();
                                    String[] sList = s.split("-");
                                    String protoName = sList[0];
                                    String value = protoNameList.get(s);
                                    if ((protoName.endsWith("Response") || protoName.endsWith("Request")) && !protoName.equals("BaseResponse")) {
                                        if (!classname.contains(value)) {
                                            continue;
                                        }
                                    } else {
                                        List<String> stringList = new ArrayList<>();
                                        for (ProtoStructureBean structureBean : protoStructureBeans) {
                                            if (structureBean.getProtoTitle().equals(value)) {
                                                stringList = structureBean.getImportTitleList();

                                            }
                                        }
                                        if (!stringList.contains(fileName)) {
                                            continue;
                                        }
                                    }
                                    if (line.matches("\\s*(message|enum)\\s+" + protoName + "[^a-zA-Z]+.*")) {

                                        ProtoStructureBean protoStructureBean = new ProtoStructureBean();
                                        protoStructureBean.setProtoTitle(fileName + "-" + protoName);
                                        protoStructureBean.setProtoName(protoName);
                                        protoStructureBean.setProtoFileName(fileName);
                                        protoStructureBean.setImportTitleList(importList);
                                        it.remove();
                                        if (protoStructureBeans.contains(protoStructureBean)) {
                                            continue;
                                        }
                                        protoStructureBeans.add(protoStructureBean);
                                        if (stringLine.get(stringLine.size() - 1).matches("\\s*//.*")) {
                                            stringBuilder.append(stringLine.get(stringLine.size() - 1) + "\n");
                                        }
                                        stringBuilder.append(line + "\n");
                                        isMatchMethod = true;
                                        if (line.contains("{")) {
                                            bracketsIndex++;
                                        }

                                    }
                                }
                                stringLine.add(line);
                            }
                            reader.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (protoNameList.size() > 0) {
            getJarName(jarFiles, protoNameList, protoStructureBeans);
        }
    }

    public static void main(String[] args) {
        String s = "OrderServiceAddOrderDetailForPiRequest";
        String[] sList = s.split("-");
        System.out.println(sList[0]);
        ProtoMethodBean protoMethodBean = new ProtoMethodBean();
        JavaTypeBean requestInfo = new JavaTypeBean();
        requestInfo.setJarPath("E:\\Maven\\repository\\com\\qingqing\\api\\protobuf-coursesvc\\1.0.0-SNAPSHOT\\protobuf-coursesvc-1.0.0-20200915.021910-285.jar");
        requestInfo.setClassType("CourseSvcClassHourV2ArrangeFormalCourseRequest");
        requestInfo.setRootClassName("CourseSvcArrangeCourseProto");
        protoMethodBean.setRequestInfo(requestInfo);
        JavaTypeBean responseInfo = new JavaTypeBean();

        responseInfo.setJarPath("E:\\Maven\\repository\\com\\qingqing\\api\\protobuf-coursesvc\\1.0.0-SNAPSHOT\\protobuf-coursesvc-1.0.0-20200915.021910-285.jar");
        responseInfo.setClassType("CourseSvcDeleteApplyInfoResponse");
        responseInfo.setRootClassName("CourseSvcDeleteWalletItemProto");
        protoMethodBean.setResponseInfo(responseInfo);
        PageEditInfo pageEditInfo = getProto(protoMethodBean);
        Gson gson = new Gson();
        System.out.println(gson.toJson(pageEditInfo));
    }

    /**
     * 主方法，获取对应proto结构
     *
     * @param protoMethodBean request和Response包装类
     */
    public static PageEditInfo getProto(ProtoMethodBean protoMethodBean) {

        try {
            if (protoMethodBean == null) {
                throw new Exception("protoMethodBean is null");
            }
            if (protoMethodBean.getRequestInfo() == null || protoMethodBean.getResponseInfo() == null) {
                throw new Exception("request or response is null,protoMethodBean:" + new Gson().toJson(protoMethodBean));
            }
            JavaTypeBean javaTypeBean = protoMethodBean.getRequestInfo();

            Map<String, String> protoMap = new HashMap<>();
            Set<String> jarFiles = new HashSet<>();
            String jarPath = javaTypeBean.getJarPath();
            if (!jarPath.contains("protobuf-base") && !protoMethodBean.getRequestInfo().getJarPath().contains("protobuf-base")) {
                jarFiles.add(getProtoFilePath(getProjectPath(jarPath)));
            }
            //构造Request
            jarFiles.add(javaTypeBean.getJarPath());
            protoMap.put(javaTypeBean.getClassType(), javaTypeBean.getRootClassName());
            //构造Response
            jarFiles.add(protoMethodBean.getResponseInfo().getJarPath());
            protoMap.put(protoMethodBean.getResponseInfo().getClassType(), protoMethodBean.getResponseInfo().getRootClassName());


            List<ProtoStructureBean> protoStructureBeansAll = new ArrayList<>();
            //获取proto结构关系
            getJarName(jarFiles, protoMap, protoStructureBeansAll);

            PageEditInfo pageEditInfo = new PageEditInfo();
            pageEditInfo.setApplicationContext(protoMethodBean.getApplicationContext());
            pageEditInfo.setMethod(protoMethodBean.getRequestMethod());
            pageEditInfo.setUrl(protoMethodBean.getApplicationContext() + protoMethodBean.getControllerUrl() + protoMethodBean.getMethodUrl());
            pageEditInfo.setTitle(protoMethodBean.getWikiTitle());
            pageEditInfo.setHeader("Content-Type: application/x-protobuf");
            pageEditInfo.setHost("http://api.changingedu.com");
            String wikiUrl = protoMethodBean.getWikiUrl();
            if (wikiUrl != null && wikiUrl.contains("=")) {
                int index = wikiUrl.indexOf("=");
                String pageId=wikiUrl.substring(index+1).trim();
                try {
                    pageEditInfo.setPageId(Long.parseLong(pageId));
                }catch (Exception e){

                }
            }
            for (ProtoStructureBean protoStructureBean : protoStructureBeansAll) {
                if ((protoStructureBean.getProtoName().endsWith("Response")) && !protoStructureBean.getProtoName().equals("BaseResponse")) {
                    //递归构造responseProto的子结构
                    setProtoStructureBeans(protoStructureBean, protoStructureBeansAll);
                    pageEditInfo.setResponseProto(protoStructureBean);
                }
                if (protoStructureBean.getProtoName().endsWith("Request")) {
                    //递归构造requestProto的子结构
                    setProtoStructureBeans(protoStructureBean, protoStructureBeansAll);
                    pageEditInfo.setRequestProto(protoStructureBean);
                }
            }
            return pageEditInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归构造proto的子结构
     *
     * @param protoStructureBean
     * @param protoStructureBeans
     */
    public static void setProtoStructureBeans(ProtoStructureBean protoStructureBean, List<ProtoStructureBean> protoStructureBeans) {

        if (protoStructureBean.getSubProtoTitles() != null && protoStructureBean.getSubProtoTitles().size() > 0) {
            for (String subProtoTitle : protoStructureBean.getSubProtoTitles()) {
                for (ProtoStructureBean structureBean : protoStructureBeans) {
                    if (structureBean.getProtoName().equals(subProtoTitle) && protoStructureBean.getImportTitleList().contains(structureBean.getProtoFileName())) {
                        List<ProtoStructureBean> protoStructureBeanList = protoStructureBean.getProtoStructureBeans();
                        if (protoStructureBeanList == null) {
                            protoStructureBeanList = new ArrayList<>();
                        }
                        if (structureBean.getSubProtoTitles() != null && structureBean.getSubProtoTitles().size() > 0) {
                            setProtoStructureBeans(structureBean, protoStructureBeans);
                        }
                        if (!protoStructureBeanList.contains(structureBean)) {
                            protoStructureBeanList.add(structureBean);
                        }
                        protoStructureBean.setProtoStructureBeans(protoStructureBeanList);
                    }
                }
            }
        }


    }

    /**
     * 获取结构体里面的子方法
     *
     * @param line
     * @param protoNameList
     * @return
     */
    public static void sonProto(String line, List<String> protoNameList, boolean isOneOf) {
        int index = line.indexOf("optional");
        if (index == -1) {
            index = line.indexOf("repeated");
        }

        if (index != -1) {
            if (line.matches(".*\\s+(bool|int32|int64|double|string)\\s+.*")) {
                return;
            }
            char[] chars = line.toCharArray();
            StringBuilder stringBuilder = new StringBuilder();
            boolean in = false;
            for (int i = 0; i < chars.length; i++) {
                if (i < index + 8) {
                    continue;
                }
                if (chars[i] == 32) {
                    if (in) {
                        break;
                    }
                    continue;
                }
                in = true;
                stringBuilder.append(chars[i]);
            }
            protoNameList.add(stringBuilder.toString());
            return;
        }
        if (isOneOf) {
            char[] chars = line.toCharArray();
            StringBuilder stringBuilder = new StringBuilder();
            boolean in = false;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == 32) {
                    if (in) {
                        break;
                    }
                    continue;
                }
                in = true;
                stringBuilder.append(chars[i]);
            }

            if (stringBuilder.toString().matches("[A-Za-z]+")) {
                protoNameList.add(stringBuilder.toString());
            }
            return;
        }
        if (line.matches("\\s*(message|enum)\\s+.*")) {
            protoNameList.removeIf(line::contains);
        }
        return;

    }

    /**
     * 获取对应Maven下的最新架包
     *
     * @param directoryPath
     * @return
     * @throws Exception
     */
    public static String getProtoFilePath(String directoryPath) throws Exception {
        String filepath = directoryPath;//file文件夹的目录
        File file = new File(filepath);//File类型可以是文件也可以是文件夹
        File[] fileList = file.listFiles();//将该目录下的所有文件放置在一个File类型的数组中
        String strFile = null;//新建一个文件集合
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile() && fileList[i].getName().endsWith("1.0.0-SNAPSHOT.jar")) {//判断是否为文件
                strFile = fileList[i].toString();
            }
        }
        if (strFile == null) {
            throw new Exception("目标架包不存在");
        }
        return strFile;

    }

    /**
     * 获取base架包所在的目录
     *
     * @param jarPath
     * @return
     * @throws Exception
     */
    public static String getProjectPath(String jarPath) throws Exception {

        int indexStart = jarPath.indexOf("protobuf-");
        int indexEnd = jarPath.indexOf("1.0.0-SNAPSHOT");
        if (indexStart < 0 || indexEnd < 0 || indexEnd <= indexStart) {
            throw new Exception("目标架包不合法");
        }
        String jarDirectory = jarPath.substring(0, indexEnd + 14);
        StringBuilder stringBuilder = new StringBuilder(jarDirectory);
        stringBuilder.replace(indexStart + 9, indexEnd - 1, "base");
        return stringBuilder.toString();
    }

}
