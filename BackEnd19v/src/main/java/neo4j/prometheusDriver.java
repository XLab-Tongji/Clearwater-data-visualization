package neo4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import com.csvreader.CsvWriter;
import org.terracotta.statistics.Time;

import static neo4j.Neo4jDriver.*;

@Component
public class prometheusDriver {

    public static ArrayList proTemp(String urlNode) {
//        query=APIServiceOpenAPIAggregationControllerQueue1_adds{instance="192.168.199.191:6443",job="kubernetes-apiservers"}
        String url = "http://10.60.38.181:31003/api/v1/query?query="+urlNode;
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
//            System.out.println(jsonText);
            JSONObject jsStr = JSONObject.parseObject(jsonText);
//            System.out.println(jsStr);
            JSONArray result = jsStr.getJSONObject("data").getJSONArray("result");
//            System.out.println(result);
            JSONArray value = ((JSONObject)result.get(0)).getJSONArray("value");
            arrayList.add(value.get(1));
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

    public static boolean DealPrometheusRequest(int startTime,int endTime,ArrayList<HashMap> requestList){
        HashMap<String,ArrayList> hashMap = new HashMap<>();
        ArrayList arrayList = new ArrayList();
        for (int i=startTime;i<endTime;i++){
            arrayList.add(i);
        }
        hashMap.put("timestamp",arrayList);
        System.out.println(requestList.size());
        for (int i=0;i<requestList.size();i++){
            String query = (String)requestList.get(i).get("sql");
            String attribute = (String)requestList.get(i).get("type");
            String serviceName = query.split("\"")[1];
            String name = "service/"+serviceName+"/"+attribute;
            System.out.println(name);
            arrayList = proTemp(query);
            hashMap.put(name,arrayList);
        }
        System.out.println(hashMap);
        csvCreate(hashMap);
        return true;
    }

    public static void csvCreate(HashMap<String,ArrayList> hashMap) {
        String outPath = "/Users/logan/Desktop/csv/";
        File csvFile = null;
        BufferedWriter csvFileOutputStream = null;
        try {
            File file = new File(outPath);
            if (!file.exists()) {
                file.mkdir();
            }
            //定义文件名格式并创建  
            csvFile = File.createTempFile(Long.toString(Time.time()), ".csv", new File(outPath));
            System.out.println("csvFile：" + csvFile);

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
            ArrayList timeStamp = hashMap.get("timestamp");
            for (int i=0;i<timeStamp.size();i++){
                for (Iterator propertyIterator = hashMap.entrySet().iterator(); propertyIterator.hasNext();) {
                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                    System.out.println(((ArrayList)propertyEntry.getValue()));

                    csvFileOutputStream.write((String) ((ArrayList)propertyEntry.getValue()).get(i).toString());
                    if (propertyIterator.hasNext()) {
                        csvFileOutputStream.write(",");
                    }
                }
                csvFileOutputStream.newLine();
            }

            csvFileOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


     public static void main(String[] args) throws IOException {
//        String query = "APIServiceOpenAPIAggregationControllerQueue1_adds{instance=\"192.168.199.191:6443\",job=\"kubernetes-apiservers\"}";
        String query = "sum(rate(request_duration_seconds_count{service=\"front-end\"}[1m]))";
        proTemp(query);
        ArrayList<HashMap> arrayList = new ArrayList<>();
        HashMap hashMap = new HashMap<String,String>();
        hashMap.put("type","response_time");
        hashMap.put("sql","sum(rate(request_duration_seconds_sum{service=\"catalogue\"}[1m]))");
        arrayList.add(hashMap);
        hashMap = new HashMap<String,String>();
        hashMap.put("type","response_time1");
        hashMap.put("sql","sum(rate(request_duration_seconds_count{service=\"catalogue\"}[1m]))");
        arrayList.add(hashMap);
        hashMap = new HashMap<String,String>();
        hashMap.put("type","success_rate");
        hashMap.put("sql","sum(rate(request_duration_seconds_count{service=\"front-end\",status_code=~\"2..\",route!=\"metrics\"}[1m]))");
        arrayList.add(hashMap);
        hashMap = new HashMap<String,String>();
        hashMap.put("type","success_rate1");
        hashMap.put("sql","sum(rate(request_duration_seconds_count{service=\"front-end\"}[1m]))");
        arrayList.add(hashMap);
        int startTime = 0;
        int endTime = 1;
        DealPrometheusRequest(startTime,endTime,arrayList);
    }

}
//sum(rate(request_duration_seconds_sum{service="catalogue"}[1m]))
//sum(rate(request_duration_seconds_count{service="catalogue"}[1m]))
//sum(rate(request_duration_seconds_count{service="front-end",status_code=~"2..",route!="metrics"}[1m]))
//sum(rate(request_duration_seconds_count{service="front-end"}[1m]))
//{service/catalogue/response_time=[0.00008038849999999856], service/front-end/success_rate=[3.333311111851827], service/catalogue/response_time1=[0.03333333333333333], timestamp=[0]}