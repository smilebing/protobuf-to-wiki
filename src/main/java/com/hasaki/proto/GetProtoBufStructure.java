package com.hasaki.proto;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hasaki.bean.ProtoStructureBean;
import cucumber.api.java.cs.A;
import cucumber.api.java.it.Ma;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
                    if (!entry.getName().contains("META-INF") || !entry.getName().contains("com")) {
                        String classFullName = entry.getName();
                        if (classFullName.endsWith(".proto")) {
                            String fileName = classFullName.substring(0, classFullName.indexOf(".proto"));
                            InputStream input = jar.getInputStream(entry);
                            InputStreamReader isr = new InputStreamReader(input);
                            BufferedReader reader = new BufferedReader(isr);
                            String line;
                            String classname = "";
                            boolean i = false;
                            List<String> stringLine = new ArrayList<>();
                            StringBuilder stringBuilder = new StringBuilder();
                            int bracketsIndex = 0;
                            List<String> sonProto = new ArrayList<>();
                            List<String> importList = new ArrayList<>();
                            importList.add(fileName);
                            while ((line = reader.readLine()) != null) {
                                if (line.contains("java_outer_classname")) {
                                    classname = line;
                                }
                                if (isImport(line, "import")) {
                                    importList.add(line.substring(line.lastIndexOf("/") + 1, line.indexOf(".proto")));
                                }
                                if (i) {
                                    if (line.contains("}")) {
                                        bracketsIndex--;
                                    }
                                    if (line.contains("{")) {
                                        bracketsIndex++;
                                    }
                                    stringBuilder.append(line + "\n");
                                    sonProto(line, sonProto);
                                    if (bracketsIndex == 0) {
                                        ProtoStructureBean protoStructureBean = protoStructureBeans.get(protoStructureBeans.size() - 1);
                                        protoStructureBean.setProtoStructure(stringBuilder.toString());
                                        if (sonProto.size() > 0) {
                                            protoStructureBean.setSubProtoTitles(sonProto);
                                            for (String s : sonProto) {
                                                protoNameList.put(s, protoStructureBean.getProtoName());
                                            }
                                        }
                                        sonProto = new ArrayList<>();
                                        stringBuilder = new StringBuilder();
                                        i = false;
                                    }
                                }
                                Iterator<String> it = protoNameList.keySet().iterator();
                                while (it.hasNext()) {
                                    String s = it.next();
                                    String value = protoNameList.get(s);
                                    if ((s.endsWith("Response") || s.endsWith("Request")) && !s.equals("BaseResponse")) {
                                        if (!classname.contains(value)) {
                                            continue;
                                        }
                                    } else {
                                        List<String> stringList = new ArrayList<>();
                                        for (ProtoStructureBean structureBean : protoStructureBeans) {
                                            if (structureBean.getProtoName().equals(value)) {
                                                stringList = structureBean.getImportTitleList();

                                            }
                                        }
                                        if (!stringList.contains(fileName)) {
                                            continue;
                                        }
                                    }
                                    if (isLine(line, s)) {
                                        ProtoStructureBean protoStructureBean = new ProtoStructureBean();
                                        protoStructureBean.setProtoName(fileName + "-" + s);
                                        protoStructureBean.setProtoTitle(s);
                                        protoStructureBean.setProtoFileName(fileName);
                                        protoStructureBean.setImportTitleList(importList);
                                        protoStructureBeans.add(protoStructureBean);

                                        stringBuilder.append(stringLine.get(stringLine.size() - 1) + "\n");
                                        stringBuilder.append(line + "\n");
                                        i = true;
                                        if (line.contains("{")) {
                                            bracketsIndex++;
                                        }
                                        it.remove();
                                        break;
                                    }
                                }
                              /*  for (String s : protoNameList) {


                                }*/
                                stringLine.add(line);
                            }
                            reader.close();
                            //  System.out.println(stringBuilder.toString());
                            // System.out.println("*****************************");
                        }
                    }
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
        if (protoNameList.size() > 0) {
            getJarName(jarFiles, protoNameList, protoStructureBeans);
        }
    }

    public static void main(String[] args) {
        List<String> classPathList = new ArrayList<>();
        classPathList.add("com.qingqing.api.coursesvc.proto.CourseSvcCourseInfoProto.CourseSvcGroupOrderCourseIdsRequest");
        classPathList.add("com.qingqing.api.coursesvc.proto.CourseSvcCourseInfoProto.CourseSvcGroupOrderCourseInfoResponse");
        List<ProtoStructureBean> protoStructureBeanList= getProto("E:\\Maven\\repository",classPathList);
        Gson gson=new Gson();
        System.out.println(gson.toJson(protoStructureBeanList));
    }

    /**
     * 主方法，获取对应proto结构
     * @param mavenPath  maven本地路径
     * @param classPathList  需要的proto 全路径
     */
    public static List<ProtoStructureBean> getProto(String mavenPath,List<String> classPathList) {
        try {
            Map<String, String> protoMap = new HashMap<>();
            Set<String> set = new HashSet<>();
            for (String s : classPathList) {
                getProjectPath(mavenPath, s, protoMap, set);
            }
            set.add(mavenPath + "\\com\\qingqing\\api\\protobuf-base\\1.0.0-SNAPSHOT");
            Set<String> jarFiles = new HashSet<>();
            for (String s : set) {
                jarFiles.add(getProtoFilePath(s));
            }
            List<ProtoStructureBean> protoStructureBeansAll = new ArrayList<>();
            getJarName(jarFiles, protoMap, protoStructureBeansAll);
            List<ProtoStructureBean> protoStructureBeans = new ArrayList<>();
            for (ProtoStructureBean protoStructureBean : protoStructureBeansAll) {
                if ((protoStructureBean.getProtoTitle().endsWith("Response") || protoStructureBean.getProtoTitle().endsWith("Request")) && !protoStructureBean.getProtoTitle().equals("BaseResponse")) {
                    setProtoStructureBeans(protoStructureBean, protoStructureBeansAll);
                    protoStructureBeans.add(protoStructureBean);
                }
            }
            return  protoStructureBeans;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static void setProtoStructureBeans(ProtoStructureBean protoStructureBean, List<ProtoStructureBean> protoStructureBeans) {

        if (protoStructureBean.getSubProtoTitles() != null && protoStructureBean.getSubProtoTitles().size() > 0) {
            for (String subProtoTitle : protoStructureBean.getSubProtoTitles()) {
                for (ProtoStructureBean structureBean : protoStructureBeans) {
                    if (structureBean.getProtoTitle().equals(subProtoTitle)) {
                        List<ProtoStructureBean> protoStructureBeanList = protoStructureBean.getProtoStructureBeans();
                        if (protoStructureBeanList == null) {
                            protoStructureBeanList = new ArrayList<>();
                        }
                        if (structureBean.getSubProtoTitles() != null && structureBean.getSubProtoTitles().size() > 0) {
                            setProtoStructureBeans(structureBean, protoStructureBeans);
                        }
                        protoStructureBeanList.add(structureBean);
                        protoStructureBean.setProtoStructureBeans(protoStructureBeanList);
                    }
                }
            }
        }


    }

    public static boolean isLine(String line, String s) {
        if (!line.contains(s)) {
            return false;
        }
        if (!(line.contains("message") || line.contains("enum"))) {
            return false;
        }
        int index = line.indexOf(s);
        char[] chars = line.toCharArray();
        char c = chars[index + s.length()];
        if ((c >= 65 & c <= 90) || (c >= 97 & c <= 122)) {
            return false;
        }
        return true;
    }

    public static boolean isImport(String line, String s) {
        if (line.indexOf(s) != 0) {
            return false;
        }
        char[] chars = line.toCharArray();
        char c = chars[s.length()];
        if (c != 32) {
            return false;
        }
        return true;
    }

    public static String sonProto(String line, List<String> protoNameList) {
        int index = line.indexOf("optional");
        if (index == -1) {
            index = line.indexOf("repeated");
        }
        if (index != -1) {
            if (line.contains("bool") || line.contains("int32") || line.contains("int64") || line.contains("double") || line.contains("string")) {
                return "";
            }
            char[] chars = line.toCharArray();
            StringBuilder stringBuilder = new StringBuilder();
            boolean in = false;
            int j = 0;
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
            return stringBuilder.toString();
        }
        if (line.contains("message")) {
            for (String s : protoNameList) {
                if (line.contains(s)) {
                    protoNameList.remove(s);
                }
            }
        }
        return "";

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
        List<File> wjList = new ArrayList<>();//新建一个文件集合
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile() && fileList[i].getName().endsWith(".jar")) {//判断是否为文件
                wjList.add(fileList[i]);
            }
        }
        if (wjList.size() <= 0) {
            throw new Exception("目标架包不存在");
        }
        if (wjList.size() > 1) {
            return wjList.get(wjList.size() - 2).toString();
        }
        return wjList.get(0).toString();

    }

    public static void getProjectPath(String mavenPath, String protoPath, Map<String, String> protoMap, Set<String> jarFiles) throws Exception {
        List<String> groupList = Arrays.asList(protoPath.split("\\."));
        if (groupList.size() != 7) {
            throw new Exception("所要获取的proto不合法");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mavenPath);
        String key = null, value = null;
        for (int i = 0; i < groupList.size(); i++) {
            if (i < 3) {
                stringBuilder.append("\\");
                stringBuilder.append(groupList.get(i));
            } else if (i == 3) {
                stringBuilder.append("\\protobuf-");
                stringBuilder.append(groupList.get(i));
                stringBuilder.append("\\1.0.0-SNAPSHOT");
            } else if (i == 5) {
                value = groupList.get(i);
            } else if (i == 6) {
                key = groupList.get(i);
            }
        }
        jarFiles.add(stringBuilder.toString());
        protoMap.put(key, value);

    }

}
