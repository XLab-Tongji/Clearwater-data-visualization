package neo4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.terracotta.statistics.Time;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static neo4j.Neo4jDriver.*;

@Component
public class prometheusDriver {

    public static ArrayList proTemp(String urlNode, String start, String end) {
//        query=APIServiceOpenAPIAggregationControllerQueue1_adds{instance="192.168.199.191:6443",job="kubernetes-apiservers"}
        String url = "http://10.60.38.181:31003/api/v1/query_range?query="+urlNode+"&start="+start+"&end="+end+"&step=10";
//        url = java.net.URLEncoder.encode(url);
        System.out.println(url);
        ArrayList arrayList = new ArrayList();
        InputStream is = null;
        try {
            is = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
//            jsonText
            System.out.println(jsonText);
            JSONObject jsStr = JSONObject.parseObject(jsonText);
//            System.out.println(jsStr);
            JSONArray result = jsStr.getJSONObject("data").getJSONArray("result");
//            System.out.println(result);
            try{
                JSONArray value = ((JSONObject)result.get(0)).getJSONArray("values");
                for (int i=0;i<value.size();i++){
                    arrayList.add(((JSONArray)value.get(i)).get(1));
                }

            }catch (Exception e){
                arrayList.add(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    public static ArrayList getProTimeStamp(String urlNode, String start, String end) {
//        query=APIServiceOpenAPIAggregationControllerQueue1_adds{instance="192.168.199.191:6443",job="kubernetes-apiservers"}
        String url = "http://10.60.38.181:31003/api/v1/query_range?query="+urlNode+"&start="+start+"&end="+end+"&step=10";
//        url = java.net.URLEncoder.encode(url);
        System.out.println(url);
        ArrayList arrayList = new ArrayList();
        InputStream is = null;
        try {
            is = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
//            jsonText
            System.out.println(jsonText);
            JSONObject jsStr = JSONObject.parseObject(jsonText);
//            System.out.println(jsStr);
            JSONArray result = jsStr.getJSONObject("data").getJSONArray("result");
//            System.out.println(result);
            try{
                JSONArray value = ((JSONObject)result.get(0)).getJSONArray("values");
                for (int i=0;i<value.size();i++){
                    arrayList.add(((JSONArray)value.get(i)).get(0));
                }
            }catch (Exception e){
                arrayList.add(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    public static boolean newPrometheus(HashMap data){
        String pUrl = (String) data.get("url");
        String nameSpace = (String) data.get("namespace");
        String serviceName = (String) data.get("service");
        System.out.println("url: "+pUrl+", service name: "+serviceName);
        return storePrometheus(pUrl, nameSpace, serviceName);
    }

    public static boolean DealPrometheusRequest(String startTime,String endTime,ArrayList<HashMap> requestList,String savePath){
        HashMap<String,ArrayList> hashMap = new HashMap<>();
        ArrayList arrayList = new ArrayList();
        arrayList = getProTimeStamp(((String)requestList.get(0).get("sql")).replace(" ","").replace("(","%28").replace(")","%29"), startTime, endTime);
        hashMap.put("TimeStamp",arrayList);

        for (int i=0;i<requestList.size();i++){
            ArrayList list = new ArrayList();
            String query = (String)requestList.get(i).get("sql");
            query = query.replace(" ","");
            query = query.replace("(","%28").replace(")","%29");
            String name = (String)requestList.get(i).get("type");
            System.out.println(name);
            list = proTemp(query, startTime, endTime);
            hashMap.put(name,list);
        }
        System.out.println(hashMap);
        String name = csvCreate(hashMap,savePath);
        System.out.println(name);
        try {
            Map map = csvPost("Fci", name);
            System.out.println(map.toString());
            String[] strings = ((String) map.get("txt")).split("\n");
            HashMap<String,ArrayList<HashMap<String,Object>>> result = causationData(strings);
            System.out.println("\n---------- result -----------");
            System.out.println(result.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static String csvCreate(HashMap<String,ArrayList> hashMap,String savePath) {
        String outPath = savePath;
        File csvFile = null;
        BufferedWriter csvFileOutputStream = null;
        try {
            File file = new File(outPath);
            if (!file.exists()) {
                file.mkdir();
            }
            //定义文件名格式并创建  
            csvFile = File.createTempFile(Long.toString(Time.time()), ".csv", new File(outPath));
            System.out.println("csvFile：" + csvFile.getName());

            for (HashMap.Entry<String, ArrayList> entry : hashMap.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);
            for (Iterator propertyIterator = hashMap.entrySet().iterator(); propertyIterator.hasNext();) {
                java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                csvFileOutputStream.write((String)propertyEntry.getKey() );
                if (propertyIterator.hasNext()) {
                    csvFileOutputStream.write(",");
                }
            }
            csvFileOutputStream.newLine();
            ArrayList timeStamp = hashMap.get("TimeStamp");
            for (int i=0;i<timeStamp.size();i++){
                for (Iterator propertyIterator = hashMap.entrySet().iterator(); propertyIterator.hasNext();) {
                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                    //System.out.println(((ArrayList)propertyEntry.getValue()));
                    try {
                        csvFileOutputStream.write((String) ((ArrayList) propertyEntry.getValue()).get(i).toString());
                    }catch (Exception e){
                        csvFileOutputStream.write("null");
                    }
                    if (propertyIterator.hasNext()) {
                        csvFileOutputStream.write(",");
                    }
                }
                csvFileOutputStream.newLine();
            }

            csvFileOutputStream.flush();

            return csvFile.getPath();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


     public static void main(String[] args) throws IOException {
//        String startTime = "1561845600";
//        String endTime = "1561849200";
//         ArrayList<HashMap> arrayList = new ArrayList<>();
//         HashMap hashMap = new HashMap<String,String>();
//         hashMap.put("type","container/catalogue-db/container_network_receive_packets");
//         hashMap.put("sql","sum(rate(container_network_receive_packets_total{image!=\"\",namespace=~\"sock-shop\",pod_name=\"catalogue-db-99cbcbb88-mw7q6\"}[5m]))");
//         HashMap hashMap1 = new HashMap<String,String>();
//         hashMap1.put("type","ss");
//         hashMap1.put("sql","sca");
//
//         arrayList.add(hashMap);
//         arrayList.add(hashMap1);
//         DealPrometheusRequest(startTime,endTime,arrayList);



//        String query = "APIServiceOpenAPIAggregationControllerQueue1_adds{instance=\"192.168.199.191:6443\",job=\"kubernetes-apiservers\"}";
//        String query = "avg(rate (container_cpu_usage_seconds_total{image!=\"\",container_name!=\"POD\",namespace=~\"sock-shop\",pod_name=~\"%s-[0-9A-Za-z]{3,}.+\"}[5m]))";
//        proTemp(query, startTime, endTime);

//        hashMap.put("type","response_time");
//        hashMap.put("sql","sum(rate(request_duration_seconds_sum{service=\"catalogue\"}[1m]))");
//        arrayList.add(hashMap);
//        hashMap = new HashMap<String,String>();
//        hashMap.put("type","response_time1");
//        hashMap.put("sql","sum(rate(request_duration_seconds_count{service=\"catalogue\"}[1m]))");
//        arrayList.add(hashMap);
//        hashMap = new HashMap<String,String>();
//        hashMap.put("type","success_rate");
//        hashMap.put("sql","sum(rate(request_duration_seconds_count{service=\"front-end\",status_code=~\"2..\",route!=\"metrics\"}[1m]))");
//        arrayList.add(hashMap);
//        hashMap = new HashMap<String,String>();
//        hashMap.put("type","success_rate1");
//        hashMap.put("sql","sum(rate(request_duration_seconds_count{service=\"front-end\"}[1m]))");

    }

}
//sum(rate(request_duration_seconds_sum{service="catalogue"}[1m]))
//sum(rate(request_duration_seconds_count{service="catalogue"}[1m]))
//sum(rate(request_duration_seconds_count{service="front-end",status_code=~"2..",route!="metrics"}[1m]))
//sum(rate(request_duration_seconds_count{service="front-end"}[1m]))

//http://10.60.38.181:31003/api/v1/query?query=sum(rate(request_duration_seconds_count{service="front-end"}[1m]))&start=0&end=1500&step=1s
//{service/catalogue/response_time=[0.00008038849999999856], service/front-end/success_rate=[3.333311111851827], service/catalogue/response_time1=[0.03333333333333333], timestamp=[0]}

//http://10.60.38.181:31003/api/v1/query_range?query=sum(rate(container_network_receive_packets_total{image!="",namespace=~"sock-shop",pod_name="catalogue-db-99cbcbb88-mw7q6"}[5m]))&start=1561845600&end=1561849200&step=10



