package com.hasaki.proto;


import com.hasaki.bean.ProtoStructureBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Description
 * @Author wangzhaoyang
 * @Date 2020/8/29 13:01
 **/
public class GetProtoBufStructure {

    public static void getJarName(List<String> jarFiles, Set<String> protoNameList, List<ProtoStructureBean> protoStructureBeans) throws Exception {

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
                            InputStream input = jar.getInputStream(entry);
                            InputStreamReader isr = new InputStreamReader(input);
                            BufferedReader reader = new BufferedReader(isr);
                            String line;
                            int index = 0;
                            boolean i = false;
                            List<String> stringLine = new ArrayList<>();
                            StringBuilder stringBuilder = new StringBuilder();
                            int bracketsIndex = 0;
                            List<String> sonProto = new ArrayList<>();
                            while ((line = reader.readLine()) != null) {
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
                                            protoNameList.addAll(new HashSet<>(sonProto));
                                        }
                                        sonProto = new ArrayList<>();
                                        stringBuilder = new StringBuilder();
                                        i = false;
                                    }
                                }
                                Iterator<String> it = protoNameList.iterator();
                                while (it.hasNext()) {
                                    String s = it.next();
                                    if (isLine(line,s)) {
                                        ProtoStructureBean protoStructureBean = new ProtoStructureBean();
                                        protoStructureBean.setProtoName(classFullName.substring(0, classFullName.indexOf(".proto")) + "-" + s);
                                        protoStructureBean.setProtoTitle(s);
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
     /*   String line = "message OrderServiceContractStatus contract_status = 3; // 合同状态";
        // int index = line.indexOf("optional");

        if(isLine(line,"OrderServiceContractStatus")) {
            System.out.println(line);
        }*/
        try {
            Set<String> list = new HashSet<>();
            list.add("CourseSvcGroupOrderCourseIdsRequest");
            list.add("CourseSvcGroupOrderCourseInfoResponse");
            Set<String> allList = new HashSet<>(list);
            List<ProtoStructureBean> protoStructureBeansAll = new ArrayList<>();
            List<String> jarFiles = new ArrayList<>();
            jarFiles.add("E:\\Maven\\repository\\com\\qingqing\\api\\protobuf-coursesvc\\1.0.0-SNAPSHOT\\protobuf-coursesvc-1.0.0-20200903.061733-261.jar");
            jarFiles.add("E:\\Maven\\repository\\com\\qingqing\\api\\protobuf-base\\1.0.0-SNAPSHOT\\protobuf-base-1.0.0-20200903.071528-94.jar");
            getJarName(jarFiles, allList, protoStructureBeansAll);
            List<ProtoStructureBean> protoStructureBeans = new ArrayList<>();
            for (ProtoStructureBean protoStructureBean : protoStructureBeansAll) {
                if (list.contains(protoStructureBean.getProtoTitle())) {
                    setProtoStructureBeans(protoStructureBean, protoStructureBeansAll);
                    protoStructureBeans.add(protoStructureBean);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setProtoStructureBeans(ProtoStructureBean protoStructureBean, List<ProtoStructureBean> protoStructureBeans) {

        if (protoStructureBean.getSubProtoTitles()!=null&&protoStructureBean.getSubProtoTitles().size()>0) {
            for (String subProtoTitle : protoStructureBean.getSubProtoTitles()) {
                for (ProtoStructureBean structureBean : protoStructureBeans) {
                    if (structureBean.getProtoTitle().equals(subProtoTitle)) {
                        List<ProtoStructureBean> protoStructureBeanList = protoStructureBean.getProtoStructureBeans();
                        if (protoStructureBeanList == null) {
                            protoStructureBeanList = new ArrayList<>();
                        }
                        if (structureBean.getSubProtoTitles()!=null&&structureBean.getSubProtoTitles().size()>0) {
                            setProtoStructureBeans(structureBean, protoStructureBeans);
                        }
                        protoStructureBeanList.add(structureBean);
                        protoStructureBean.setProtoStructureBeans(protoStructureBeanList);
                    }
                }
            }
        }


    }

    public static boolean  isLine(String line,String s){
        if(!line.contains(s)){
            return  false;
        }
        if(!(line.contains("message")|| line.contains("enum"))){
            return  false;
        }
       int index= line.indexOf(s);
        char[] chars=line.toCharArray();
        char c=chars[index+s.length()];
        if((c>=65&c<=90)||(c>=97&c<=122)){
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

}
