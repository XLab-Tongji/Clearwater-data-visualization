package service;


import com.csvreader.CsvReader;
import global.globalvalue;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvService {
    public static ArrayList csvTimestamp(String filePath) {
        ArrayList arrayList = new ArrayList();
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            csvReader.readHeaders();
            while (csvReader.readRecord()){
                //System.out.println(csvReader.get("timestamp"));
                String string = csvReader.get("timestamp");
                if (!string.equals(""))
                    arrayList.add(string);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }
    public static ArrayList CurrentArray(String header,String filePath){
        try {
            ArrayList arrayList = new ArrayList();
            CsvReader csvReader = new CsvReader(filePath);
            csvReader.readHeaders();
            while (csvReader.readRecord()){
                String string = csvReader.get(header);
                if (!string.equals("")){
                    arrayList.add(string);
                }
            }
            return arrayList;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static HashMap<String,ArrayList> csvOperationData(String filePath) {
        HashMap<String,ArrayList> hashMap = new HashMap();
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            String header[] = null;
            while (csvReader.readRecord()){
                header = csvReader.getRawRecord().split(",");
                break;
            }
            csvReader.close();
            csvReader = new CsvReader(filePath);
            csvReader.readHeaders();
            try {
                for (int i = 1; i < header.length; i++) {
                    hashMap.put(header[i],CurrentArray(header[i],filePath));
                    //hashMap.put(header[i], arrayList);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMap;

    }
    public static HashMap<String, HashMap<String,Object>> metricInfo(String filePath){
        HashMap<String, HashMap<String,Object>> hashMap = new HashMap<>();

        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            String header[] = null;
            while (csvReader.readRecord()) {
                header = csvReader.getRawRecord().split(",");
                break;
            }
            //container和dataset下次改
            HashMap<String,Object> hashMapTemp = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add("service");
            list.add("deployment");
            list.add("container");
            hashMapTemp.put("name",list);
            hashMap.put("Node",hashMapTemp);
            hashMap.put("service",new HashMap<>());
            hashMap.put("deployment",new HashMap<>());
            hashMap.put("container",new HashMap<>());
            hashMap.get("container").put("place",new ArrayList<>());
            hashMap.get("service").put("place",new ArrayList<>());
            hashMap.get("deployment").put("place",new ArrayList<>());

            for (int i=0;i<header.length;i++){
                try{
                    String temp[] = header[i].split("/");
                    if (temp[0].equals("container")){
                        HashMap<String,Object> hashMap1 = hashMap.get("container");
                        Boolean flag = hashMap1.containsKey(temp[1]);
                        if (!flag){
                            ArrayList arrayList = (ArrayList)hashMap1.get("place");
                            arrayList.add(temp[1]);

                            ArrayList arrayList1 = new ArrayList();
                            hashMap1.put(temp[1],arrayList1);
                            arrayList1.add(temp[2]);

                        }else{
                            ArrayList arrayList = (ArrayList)hashMap1.get(temp[1]);
                            arrayList.add(temp[2]);
                        }
                    }else if (temp[0].equals("service")){
                        HashMap<String,Object> hashMap1 = hashMap.get("service");
                        String temp2[] = temp[1].split("_");
                        Boolean flag = hashMap1.containsKey(temp2[0]);
                        if (!flag){
                            ArrayList arrayList = (ArrayList)hashMap1.get("place");
                            arrayList.add(temp2[0]);

                            ArrayList arrayList1 = new ArrayList();
                            hashMap1.put(temp2[0],arrayList1);
                            arrayList1.add(temp[2]);
                        }else{
                            ArrayList arrayList = (ArrayList)hashMap1.get(temp2[0]);
                            arrayList.add(temp[2]);
                        }
                    }else if (temp[0].equals("deployment")){

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return hashMap;
    }
    public static Map<String, Object> csvPost(String algorithm, String filepath) throws IOException {
        String sURL = globalvalue.causeapi + ":10080/causality";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(sURL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("algorithm", algorithm, ContentType.TEXT_PLAIN);
        builder.addTextBody("format", "json", ContentType.TEXT_PLAIN);

        // 把文件加到HTTP的post请求中
        File f = new File(filepath);
        builder.addBinaryBody(
                "file",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        String sResponse= EntityUtils.toString(responseEntity, "UTF-8");
        Map maps = (Map) com.alibaba.fastjson.JSON.parse(sResponse);
        Map maps2 = (Map) com.alibaba.fastjson.JSON.parse(jsonPost(algorithm,filepath));
        System.out.println(maps2);
        maps.put("txt",maps2.get("data"));
        return maps;
    }
    public static String jsonPost(String algorithm,String filepath) throws IOException {
        String sURL = "http://10.60.38.173:10080/causality";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(sURL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("algorithm", algorithm, ContentType.TEXT_PLAIN);
        builder.addTextBody("format", "text", ContentType.TEXT_PLAIN);

        // 把文件加到HTTP的post请求中
        File f = new File(filepath);
        builder.addBinaryBody(
                "file",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        String sResponse= EntityUtils.toString(responseEntity, "UTF-8");
        return sResponse;
    }
    public static HashMap<String,ArrayList<HashMap<String,Object>>> causationData(String[] strings){
        try {
            HashMap<String, ArrayList<HashMap<String, Object>>> resultgraph = new HashMap<>();
            ArrayList<HashMap<String, Object>> allnodes = new ArrayList<>();
            ArrayList<HashMap<String, Object>> allrelations = new ArrayList<>();
//            File file = new File(filePath);
            for (int i=0;i<strings.length;i++){
                System.out.println(strings[i]);
            }
            int lineNum = 1;

            String line = strings[lineNum];
            int source = 0;
            int target = 0;

            String node[] = line.split(";");
            //System.out.println(line);

            for (int i = 1; i < node.length; i++) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("Name", node[i]);
                if (!allnodes.contains(map))
                    allnodes.add(map);
            }

            lineNum=4;
            line = strings[lineNum];

            while (lineNum<strings.length-1) {
                //System.out.println(line);
                String string[] = line.split(" ");
                HashMap<String, Object> map = new HashMap<>();
//                System.out.println(string[1]+"|"+string[3]);
//                System.out.println(string[2]);

                map.put("source", string[1]);
                map.put("target", string[3]);
                //source = AddCausationNode(string[1]);
                //target = AddCausationNode(string[3]);
                source = 1;
                target = 1;
                if (source!=0 && target!=0) {
                    String relationship = "";
                    if (string[2].equals("-->") || string[2].equals("o->"))
                        relationship = "casues";
                    if (string[2].equals("o-o"))
                        relationship = "has_causation";
                    if (string[2].equals("<->"))
                        relationship = "has_latent_variable";
//                    if (relationship.equals("has_latent_variable")) {
//                        AddMetricRelation(source, target, "has_latent_variable");
//                        AddMetricRelation(target, source, "has_latent_variable");
//                    } else {
//                        AddMetricRelation(source, target, relationship);
//                    }
                    map.put("type", relationship);
                    if (!allrelations.contains(map))
                        allrelations.add(map);
                }
                source=0;
                target=0;
                lineNum++;
                if (lineNum<strings.length)
                    line = strings[lineNum];
            }
            resultgraph.put("nodes", allnodes);
            resultgraph.put("relations", allrelations);
            return resultgraph;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
