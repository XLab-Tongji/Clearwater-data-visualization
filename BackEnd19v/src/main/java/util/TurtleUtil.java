package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TurtleUtil {
    public static void readOntologyJson(String fileName) throws IOException {
        File fileOntology = new File(TurtleUtil.class.getResource("/").getPath().replace("classes","upload/type")+fileName+".json");
        String ontologyData = FileUtils.readFileToString(fileOntology);
        JSONObject ontology=JSON.parseObject(ontologyData);
        String savePath = TurtleUtil.class.getResource("/").getPath().replace("classes","turtle/type");
        File folder = new File(savePath);
        //文件夹路径不存在
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        File ontologyTtlFile=new File(savePath + fileName + ".ttl");
        if(!ontologyTtlFile.exists()){
            ontologyTtlFile.createNewFile();
        }
        FileWriter ontologyTtlWriter =new FileWriter(ontologyTtlFile);
        //命名空间声明
        ontologyTtlWriter.write("@prefix : <http://localhost/KGns/#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix rel: <http://localhost/KGns/relationship#> .\n" +
                "@prefix Container_attributes: <http://localhost/KGns/Container_attributes#> .\n" +
                "@prefix Service_attributes: <http://localhost/KGns/Service_attributes#> .\n\n");
        ontologyTtlWriter.flush();
        ontologyJsonToTurtle(ontology,ontologyTtlWriter);

    }

    public static void ontologyJsonToTurtle(JSONObject object,FileWriter writer) throws IOException {
        JSONArray types = object.getJSONArray("types");
        List<String>attributeList=new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String type = types.getString(i);
            writer.write(":" + type + " rdf:type owl:Class .\n");
            writer.flush();
        }
        writer.write("\n");
        writer.flush();
        JSONArray relInfo = object.getJSONArray("rel_info");
        for (int i = 0; i < relInfo.size(); i++) {
            JSONObject rel = relInfo.getJSONObject(i);
            String relType = rel.getString("type");
            String domain=relType+"_attributes";
            JSONArray linkNodes = rel.getJSONArray("link_to_other_nodes");
            for (int j = 0; j < linkNodes.size(); j++) {
                JSONObject obj = linkNodes.getJSONObject(j);
                String relation = obj.getString("relation");
                String to = obj.getString("to");
                writer.write("rel:" + relation + " rdf:type owl:ObjectProperty;\n\t\t");
                writer.flush();
                writer.write("rdfs:domain :" + relType + ";\n\t\t");
                writer.flush();
                writer.write("rdfs:range :" + to + " .\n\n");
            }
            JSONArray attributes = rel.getJSONArray("attributes");
            for (int k = 0; k < attributes.size(); k++) {
                String attribute=attributes.getString(k);
                attributeList.add(domain+"|"+attribute+"|"+relType);
            }
        }
        for (String s:attributeList){
            String[]input=s.split("\\|");
            writer.write(input[0]+":"+input[1]+" rdf:type owl:ObjectProperty;\n\t\trdfs:domain :"+input[2]+" .\n\n");
            writer.flush();
        }
    }

    public static  void readSystemJson(String fileName) throws IOException {
        File fileSystem = new File(TurtleUtil.class.getResource("/").getPath().replace("classes","upload/system")+fileName+".json");
        String systemData = FileUtils.readFileToString(fileSystem);
        JSONObject system=JSON.parseObject(systemData);
        String savePath = TurtleUtil.class.getResource("/").getPath().replace("classes","turtle/system");
        File folder = new File(savePath);
        //文件夹路径不存在
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        File systemTtlFile=new File(savePath + fileName + ".ttl");
        if(!systemTtlFile.exists()){
            systemTtlFile.createNewFile();
        }
        FileWriter systemTtlWriter =new FileWriter(systemTtlFile);
        //命名空间声明
        systemTtlWriter.write("@prefix : <http://localhost/KGns/#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n" +
                "@prefix rel: <http://localhost/KGns/relationship#> .\n" +
                "@prefix Container_attributes: <http://localhost/KGns/Container_attributes#> .\n" +
                "@prefix Service_attributes: <http://localhost/KGns/Service_attributes#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n\n");
        systemTtlWriter.flush();
        systemJsonToTurtle(system,systemTtlWriter);
    }

    public static void systemJsonToTurtle(JSONObject object,FileWriter writer) throws IOException {
        List<String> instanceTypes = new ArrayList<>();
        HashMap<String, String> instanceNames = new HashMap<>();
        //写入 处理第一部分
        for (String instanceType : object.keySet()) {
            instanceTypes.add(instanceType);
            JSONArray instances = object.getJSONArray(instanceType);
            for (int i = 0; i < instances.size(); i++) {
                JSONObject instance = instances.getJSONObject(i);
                //节点名字
                String name = instance.getString("name");
                instanceNames.put(name, instanceType);
            }
        }
        for (int i = 0; i < instanceTypes.size(); i++) {
            writer.write(":" + instanceTypes.get(i) + " rdf:type owl:Class .\n");
            writer.flush();
        }
        for (String key : instanceNames.keySet()) {
            String value = instanceNames.get(key);
            writer.write(":" + key + " rdf:type :" + value + " .\n");
            writer.flush();
        }
        writer.write("\n");
        writer.flush();
        for (String instanceType : object.keySet()) {
            JSONArray instances = object.getJSONArray(instanceType);
            for (int i = 0; i < instances.size(); i++) {
                JSONObject instance = instances.getJSONObject(i);
                //节点名字
                String name = instance.getString("name");
                writer.write(":" + name + "\n\t\t");
                writer.flush();
                //属性
                JSONObject attributes=instance.getJSONObject("attributes");
                for(String key:attributes.keySet()){
                    String attribute=attributes.getString(key);
                    writer.write(instanceType+"_attributes:"+key+" "+attribute+";\n\t\t");
                    writer.flush();
                }
                JSONObject relations=instance.getJSONObject("relations");
                for (String k : relations.keySet()) {
                    List keyList=new ArrayList(relations.keySet());
                    String lastKey=keyList.get(keyList.size()-1).toString();
                    Object v = relations.get(k);
                    if (v instanceof JSONObject) {
                        for (String key :((JSONObject)v).keySet()){
                            JSONArray array=((JSONObject)v).getJSONArray(key);
                            for(int j=0;j<array.size();j++){
                                String relation=array.getString(j);
                                writer.write("rel:"+k+" :"+relation);
                                writer.flush();
                                if(j==array.size()-1&&k==lastKey){
                                    writer.write(" .\n\n");
                                    writer.flush();
                                }
                                else {
                                    writer.write(";\n\t\t");
                                    writer.flush();
                                }
                            }
                        }
                    }
                    else{
                        String relation=v.toString();
                        writer.write("rel:"+k+" :"+relation);
                        writer.flush();
                        if(k==lastKey){
                            writer.write(" .\n\n");
                            writer.flush();
                        }
                        else {
                            writer.write(";\n\t\t");
                        }
                    }
                }

            }
        }
    }


    public static void main(String[] args) throws IOException {
//        readOntologyJson();
//        readSystemJson();
    }
}