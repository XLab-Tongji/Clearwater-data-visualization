package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

import static javax.xml.transform.OutputKeys.ENCODING;

public class JsonUtil {

    Map map=new HashMap();

    public String jsonToXml(JSONObject jObj){
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            jsonToXmlstr(jObj,buffer);
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String jsonToXmlstr(JSONObject jObj,StringBuffer buffer ){
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        for(Iterator<Map.Entry<String, Object>> it = se.iterator(); it.hasNext(); )
        {
            Map.Entry<String, Object> en = it.next();
            if(en.getValue()instanceof JSONObject){
                buffer.append("<"+en.getKey()+" "+map.get(en.getKey())+">");
                JSONObject jo = jObj.getJSONObject(en.getKey());
                jsonToXmlstr(jo,buffer);
                buffer.append("</"+en.getKey()+" "+map.get(en.getKey())+">");
            }else if(en.getValue()instanceof JSONArray){
                JSONArray jarray = jObj.getJSONArray(en.getKey());
                for (int i = 0; i < jarray.size(); i++) {
                    buffer.append("<"+en.getKey()+" "+map.get(en.getKey())+">");
                    JSONObject jsonobject =  jarray.getJSONObject(i);
                    jsonToXmlstr(jsonobject,buffer);
                    buffer.append("</"+en.getKey()+" "+map.get(en.getKey())+">");
                }
            }else{
                buffer.append("<"+en.getKey()+" "+map.get(en.getKey())+">"+en.getValue());
                buffer.append("</"+en.getKey()+" "+map.get(en.getKey())+">");
            }
        }
        return buffer.toString();
    }

    public void readJson()
    {
        File file = new File("F:\\Xlab\\test.json");
        try {
            String data = FileUtils.readFileToString(file);
            JSONArray array=JSON.parseArray(data);
            for(int i=0;i<array.size();i++){
                JSONObject object=array.getJSONObject(i);
                //Map<String, Object> myResult =parseJSON2Map(object);
                String xmlstr = jsonToXml(object);
                System.out.println(xmlstr);


            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    public Map<String, Object> parseJSON2Map(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是json数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                for (int i=0;i<((JSONArray) v).size();i++) {
                    JSONObject json2 =((JSONArray) v).getJSONObject(i);
                    list.add(parseJSON2Map(json2));
                }
                map.put(k.toString(), list);
            } else if (v instanceof JSONObject) {
                // 如果内层是json对象的话，继续解析
                map.put(k.toString(), parseJSON2Map((JSONObject) v));
            } else {
                // 如果内层是普通对象的话，直接放入map中
                map.put(k.toString(), v);
            }
        }
        return map;
    }
     */

    public Element readXmlRoot(String soucePath){
        try {
            File file = new File(soucePath);
            SAXReader read = new SAXReader();
            org.dom4j.Document doc = read.read(file);
            Element root = doc.getRootElement();
            return root;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    public void getNodes(Element node){
        //当前节点的名称、文本内容和属性
        String name=node.getName();
        String context=node.getTextTrim();
        if(context!=null){
            map.put(context,name);
        }
        //List<Attribute> listAttr=node.attributes();//当前节点的所有属性的list
        /*
        for(Attribute attr:listAttr){//遍历当前节点的所有属性

            String name=attr.getName();//属性名称
            String value=attr.getValue();//属性的值
            System.out.println("属性名称："+name+"属性值："+value);
        }
         */

        //递归遍历当前节点所有的子节点
        List<Element> listElement=node.elements();//所有一级子节点的list
        for(Element e:listElement){//遍历所有一级子节点
            getNodes(e);//递归
        }
    }

     public void readXMLModel(){
        Element root=readXmlRoot("F:\\Xlab\\testModel.XML");
        Map map=new HashMap();
        getNodes(root);



    }


    public static void main(String[] args) {
        JsonUtil exm=new JsonUtil();
        exm.readXMLModel();
        exm.readJson();
    }
}


