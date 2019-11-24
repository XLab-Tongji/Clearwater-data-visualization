package neo4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neo4jentities.DataAccessor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.commons.codec.binary.Base64;

import org.apache.jena.riot.web.HttpOp;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import global.globalvalue;

import static neo4j.MongoDriver.*;
import static neo4j.prometheusDriver.getProInfor;
import static util.FileUtil.saveClusterResult;
import static util.HttpPostUtil.getImage;


@Component
public class FusekiDriver {

    public static Map<String, Object> getAllNodesAndLinks(){
        Map<String, Object> final_list = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> eventList = new ArrayList<>();
        List<Map<String, Object>> linkList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");

        Query query = QueryFactory.create("SELECT distinct ?s WHERE {\n" +
                "\t?s ?p ?o\n" +
                "}");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query);
            ResultSet rs = qExec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next() ;
                String subject = qs.get("s").toString();
                if(subject.contains("http")){
                    //System.out.println("Subject: " + subject);
                    if(subject.contains("server")){
                        result.add(getServer(subject));
                        linkList.addAll(getLink(subject, "manage"));
                    }
                    else if(subject.contains("timestamp")){
                        result.add(getTimestamp(subject));
                    }
                    else if(subject.contains("environment")){
                        result.add(getEnv(subject));
                        linkList.addAll(getLink(subject, "has"));
                    }
                    else if(subject.contains("namespace")){
                        result.add(getNamespace(subject));
                        linkList.addAll(getLink(subject, "supervises"));
                    }
                    else if(subject.contains("event")){
                        Map<String, Object> event = getEventNodes(subject);
                        result.add(event);
                        eventList.add(event);
                        linkList.addAll(getLink(subject, "starts_at"));
                        linkList.addAll(getLink(subject, "ends_at"));
                    }
                    else if(subject.contains("service")){
                        if(subject.contains("db")){
                            if (subject.contains("latency")||subject.contains("throughput"))
                                result.add(getPropertyNodes(subject, "serviceDatabase"));
                            else{
                                result.add(getService(subject));
                                linkList.addAll(getLink(subject, "profile"));
                            }
                        }
                        else {
                            if (subject.contains("success_rate")||subject.contains("latency"))
                                result.add(getPropertyNodes(subject, "serviceServer"));
                            else{
                                result.add(getService(subject));
                                linkList.addAll(getLink(subject, "profile"));
                            }
                        }
                    }
                    else if(subject.contains("pods")){
                        result.add(getPod(subject));
                        linkList.addAll(getLink(subject, "deployed_in"));
                        linkList.addAll(getLink(subject, "contains"));
                        linkList.addAll(getLink(subject, "provides"));
                    }
                    else if(subject.contains("containers")){
                        if (subject.contains("CPU_Usage")||subject.contains("MEM_Usage")){
                            result.add(getPropertyNodes(subject, "containerStorage"));
                        }else if (subject.contains("Network_Input_Bytes")||subject.contains("Network_Output_Bytes")||subject.contains("Network_Input_Packets")||subject.contains("Network_Output_Packets")){
                            result.add(getPropertyNodes(subject, "containerNetwork"));
                        }else {
                            result.add(getContainer(subject));
                            linkList.addAll(getLink(subject, "profile"));
                        }
                    }
                }
            }
            qExec.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            timeList = getTimesFromMongo();
        } catch (Exception e){
            e.printStackTrace();
        }
        final_list.put("nodeList", result);
        final_list.put("eventList", eventList);
        final_list.put("linkList", linkList);
        final_list.put("timeList", timeList);
        return final_list;
    }

    public static Map<String, Object> getAllByTime(String time){
        return getOneFromMongo(time);
    }

    public static boolean addNode(HashMap data){
        try {
            String url = data.get("id").toString();
            String[] temp = url.split("/");
            String urltemp = url.replace("/"+temp[temp.length-1],"");
            System.out.println(urltemp);
            HashMap property = (HashMap) data.get("property");

            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(url);
            try{
                if (judgeExist(url)){
                    resource.addProperty(model.createProperty(urltemp, "/name"),(String)data.get("name"));
                    resource.addProperty(model.createProperty(url,"/type"),(String)data.get("type"));
                    System.out.println(data.get("name"));
                    for (Object key : property.keySet()) {
                        resource.addProperty(model.createProperty(url, "/" + key), (String) property.get(key));
                    }
                    DataAccessor.getInstance().add(model);
                }
                else {
                    System.out.println("same name");
                    return false;
                }
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean readJekins(HashMap data){

        String fullurl = ((HashMap)data.get("build")).get("full_url").toString();
        String[] list = fullurl.split("/");
        String name = data.get("name").toString();
        name += "_";
        name += list[list.length-1];
        System.out.println(name);
        String number = String.valueOf((int)((HashMap)data.get("build")).get("number"));
        String status = ((HashMap)data.get("build")).get("status").toString();
        String phase= ((HashMap)data.get("build")).get("phase").toString();
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        String jsonString = getData(httpUrl);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
        String serviceUrl = "http://event/"+address+"/"+name;
        if (phase.equals("FINALIZED")){
            // 创建Timestamp节点 返回id
            Date day=new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(day);
            // 创建Timestamp节点 返回id
            String timeID = storeTimestamp(time);
//            String eventID = findEvent(serviceUrl);
            String eventID = serviceUrl;
            System.out.println("-----------"+eventID);
            //连接
            HashMap hashMap = new HashMap();
            hashMap.put("sid",eventID);
            hashMap.put("tid",timeID);
            hashMap.put("type","ends_at");
            addLink2(hashMap, true, time);
        }else{
            Date day=new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(day);
            // 创建Timestamp节点 返回id
            String timeID = storeTimestamp(time);
            HashMap map = new HashMap();
            map.put("full_url",fullurl);
            map.put("name",name);
            map.put("number",number);
            map.put("status",status);
            String eventID = addEvent(map);
            //连接
            HashMap hashMap = new HashMap();
            hashMap.put("sid",eventID);
            hashMap.put("tid",timeID);
            hashMap.put("type","starts_at");
            addLink2(hashMap, true, time);

        }
        return true;
    }

    public static boolean readKapacitor(HashMap data){
        String address = "10.60.38.173";
        String message = data.get("message").toString();
        //判断有没有type字段，没有则加一个
        if (!data.containsKey("type")) {
            data.put("type", "alert");
        }
        String serviceUrl = "http://event/" + address + "/" + message;
        Date day=new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(day);
        // 创建Timestamp节点 返回id
        String timeID = storeTimestamp(time);
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(serviceUrl);
            System.out.println(serviceUrl);
            System.out.println("-------");
            for (Object key : data.keySet().toArray()
                 ) {
                key = key.toString();
                resource.addProperty(model.createProperty(serviceUrl, "/" + key), data.get(key).toString() );
            }
            DataAccessor.getInstance().add(model);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        HashMap hashMap = new HashMap();
        hashMap.put("sid",serviceUrl);
        hashMap.put("tid",timeID);
        hashMap.put("type","time");
        addLink2(hashMap, true, time);
        return true;
    }

    public static String addEvent(HashMap data){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        String jsonString = getData(httpUrl);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
        String full_url = data.get("full_url").toString();
        String name = data.get("name").toString();
        String number = data.get("number").toString();
        String status = data.get("status").toString();
        String serviceUrl = "http://event/"+address+"/"+name;
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(serviceUrl);
            System.out.println(serviceUrl);
            System.out.println("-------");
            resource.addProperty(model.createProperty(serviceUrl, "/full_url"), full_url);
            resource.addProperty(model.createProperty(serviceUrl, "/name"), name);
            resource.addProperty(model.createProperty(serviceUrl, "/nubmer"), number);
            resource.addProperty(model.createProperty(serviceUrl, "/status"), status);
            DataAccessor.getInstance().add(model);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return serviceUrl;
    }

    public static boolean addLink2(HashMap data, boolean flag, String time){
        String fromUrl = (String)data.get("sid");
        String toUrl = (String)data.get("tid");
        String type = (String)data.get("type");
        System.out.println(fromUrl);
        System.out.println(toUrl);
        System.out.println(type);
        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
                "INSERT DATA{\n" +
                "<"+fromUrl+"> j0:"+type +" <"+toUrl+">\n" +
                "}";
        System.out.println(addRelation);

        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/update");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        HttpOp.setDefaultHttpClient(httpclient);
        builderAddRelation.httpClient(httpclient);

        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
            connAddRelation.update(addRelation);
            Map<String, Object> result = getAllNodesAndLinks();
            if (flag){
                return save2MongoByTime(result, time);
            }
            else {
                return save2Mongo(result);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addLink(HashMap data){
        return addLink2(data, false, "");
//        String fromUrl = (String)data.get("sid");
//        String toUrl = (String)data.get("tid");
//        String type = (String)data.get("type");
//        System.out.println(fromUrl);
//        System.out.println(toUrl);
//        System.out.println(type);
//        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
//                "INSERT DATA{\n" +
//                "<"+fromUrl+"> j0:"+type +" <"+toUrl+">\n" +
//                "}";
//        System.out.println(addRelation);
//
//        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
//                .destination("http://10.60.38.173:3030/DevKGData/update");
//
//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
//        credsProvider.setCredentials(AuthScope.ANY, credentials);
//        HttpClient httpclient = HttpClients.custom()
//                .setDefaultCredentialsProvider(credsProvider)
//                .build();
//        HttpOp.setDefaultHttpClient(httpclient);
//        builderAddRelation.httpClient(httpclient);
//
//        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
//            connAddRelation.update(addRelation);
//            Map<String, Object> result = getAllNodesAndLinks();
//            if(!save2Mongo(result)) return false;
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//        return true;

    }

    public static boolean deleteLinks(List<HashMap> data){
        try {
            for (HashMap link: data) {
                String sid = (String)link.get("sid");
                String tid = (String)link.get("tid");
                if(!deleteOneLink(sid, tid)) return false;
            }
            Map<String, Object> result = getAllNodesAndLinks();
            if(!save2Mongo(result)) return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteNodes(List<HashMap> data){
        try {
            for (HashMap link: data) {
                String id = (String)link.get("id");
                if(!deleteOneNode(id)) return false;
            }
            Map<String, Object> result = getAllNodesAndLinks();
            if(!save2Mongo(result)) return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteAll(){
        try {
            RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                    .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
            String deleteAll = "DELETE WHERE\n" +
                    "{\n" +
                    "\t?s ?p ?o .\n" +
                    "}";

//// first we use a method on HttpOp to log in and get our cookie
//            Params params = new Params();
//            params.addParam("username", "admin");
//            params.addParam("password", "D0rlghQl5IAgYOm");
//            HttpOp.execHttpPostForm("http://10.60.38.173:3030", params , null, null, null, httpContext);
//
//// now our cookie is stored in httpContext
//            CookieStore cookieStore = httpContext.getCookieStore();
//
//// lastly we build a client that uses that cookie
//            HttpClient httpclient = HttpClients.custom()
//                            .setDefaultCookieStore(cookieStore)
//                            .build();
//            HttpOp.setDefaultHttpClient(httpclient);

            System.out.println(Base64.class.getProtectionDomain().getCodeSource().getLocation());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
            credsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();
            HttpOp.setDefaultHttpClient(httpclient);
            builderAddRelation.httpClient(httpclient);


            try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                connAddRelation.update(deleteAll);
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean modifyNode(HashMap data){
        try {
            String url = data.get("id").toString();
            boolean flag = deleteOneNode(url);
            if (!flag){
                return flag;
            }else if(addNode(data)){
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }
            else return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean modifyLink(HashMap data){
        try {
            String fromUrl = (String)data.get("sid");
            String toUrl = (String)data.get("tid");
            boolean flag = deleteOneLink(fromUrl,toUrl);
            if (!flag){
                return flag;
            }else if(addLink(data)){
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }
            else return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }


    public static String getData(String address) {
        URL url = null;
        HttpURLConnection httpConn = null;
        BufferedReader in = null;

        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(address);
            in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
            String str = null;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (in != null) {

                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String data = sb.toString();
        return data;
    }

    public static boolean storeNamespace() {
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_namespace";

        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            Model model = ModelFactory.createDefaultModel();
            String url = "http://namespace/" + address + "/";
            Resource resource = model.createResource(url);

            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    String name = (String) jObject.getJSONObject("metadata").get("name");
//                if (name.equals("monitoring") || name.equals("sock-shop")) {
                    resource.addProperty(model.createProperty(url + name),
                            model.createResource().addProperty(model.createProperty(url + name + "/status"), (String) jObject.getJSONObject("status").get("phase")));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            //存储fuseki
            DataAccessor.getInstance().add(model);
            model.write(System.out, "RDF/XML-ABBREV");
            model.write(System.out, "N-TRIPLE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeNamespaceName(){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        try {
            // get address string
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            try {
                // create model
                Model model = ModelFactory.createDefaultModel();
                // namespace string
                String np1 = "http://namespace/"+address+"/monitoring";
                String np2 = "http://namespace/"+address+"/sock-shop";
                Resource res = model.createResource(np1);
                Resource resource = model.createResource(np2);
                res.addProperty(model.createProperty(np1+"/name"), "monitoring");
                resource.addProperty(model.createProperty(np2+"/name"), "sock-shop");
                res.addProperty(model.createProperty(np1, "/supervises"), resource);
                //存储fuseki
                model.write(System.out, "RDF/XML-ABBREV");
                DataAccessor.getInstance().add(model);
                model.write(System.out, "N-TRIPLE");

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static String storeTimestamp(String time){
        String np = "";
        try {
            String address = "10.60.38.181";
            String[] l = time.split(" ");
            String t = l[0]+"-"+l[1];
            np = "http://timestamp/"+address+"/"+t;
            System.out.println("storing timestamp node: "+np);
            Model model = ModelFactory.createDefaultModel();
            try {
                Resource res = model.createResource(np);
                res.addProperty(model.createProperty(np+"/attr"), "none");
                DataAccessor.getInstance().add(model);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return np;
    }

    public static boolean storeServerName(){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            String nodeName = "";
            for (int i=0;i<jsonArray.size();i++){
                try {
                    JSONObject jMetaData = jsonArray.getJSONObject(i).getJSONObject("metadata");
                    JSONObject jSpec = jsonArray.getJSONObject(i).getJSONObject("spec");
                    JSONObject jStatus = jsonArray.getJSONObject(i).getJSONObject("status");

                    Model model = ModelFactory.createDefaultModel();

                    String store = "http://backup/"+address;
                    Resource res = model.createResource(store);
                    res.addProperty(model.createProperty(store+"/server"), (String) jMetaData.get("name"));

                    String names = "http://server/" + address + "/"+(String) jMetaData.get("name");
                    Resource resource = model.createResource(names);
                    resource.addProperty(model.createProperty(names+"/name"), (String) jMetaData.get("name"));
                    resource.addProperty(model.createProperty(names, "/labels"),model.createResource()
                            .addProperty(model.createProperty(names,"/labels/beta.kubernetes.io/arch"),
                                    (String) jMetaData.getJSONObject("labels").get("beta.kubernetes.io/arch"))
//                            .addProperty(model.createProperty(names,"/labels/beta.kubernetes.io/fluentd-ds-ready"),
//                                    (String) jMetaData.getJSONObject("labels").get("beta.kubernetes.io/fluentd-ds-ready"))
                            .addProperty(model.createProperty(names,"/labels/beta.kubernetes.io/os"),
                                    (String) jMetaData.getJSONObject("labels").get("beta.kubernetes.io/os"))
                            .addProperty(model.createProperty(names,"/labels/kubernetes.io/hostname"),
                                    (String) jMetaData.getJSONObject("labels").get("kubernetes.io/hostname"))
                            .addProperty(model.createProperty(names,"/labels/kubernetes.io/role"),
                                    (String) jMetaData.getJSONObject("labels").get("kubernetes.io/role")));
                    resource.addProperty(model.createProperty(names, "/annotations"),model.createResource()
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/backend-data"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/backend-data"))
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/backend-type"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/backend-type"))
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/kube-subnet-manager"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/kube-subnet-manager"))
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/public-ip"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/public-ip"))
                            .addProperty(model.createProperty(names,"/annotations/node.alpha.kubernetes.io/ttl"),
                                    (String) jMetaData.getJSONObject("annotations").get("node.alpha.kubernetes.io/ttl"))
                            .addProperty(model.createProperty(names,"/annotations/volumes.kubernetes.io/controller-managed-attach-detach"),
                                    (String) jMetaData.getJSONObject("annotations").get("volumes.kubernetes.io/controller-managed-attach-detach")));
                    resource.addProperty(model.createProperty(names, "/podCIDR"), (String) jSpec.get("podCIDR"));
                    resource.addProperty(model.createProperty(names, "/creationTimestamp"), (String) jMetaData.get("creationTimestamp"));
                    List<JSONObject> jConditions = (List<JSONObject>) jStatus.get("conditions");
//                    System.out.println(jConditions.size());
                    for (JSONObject json: jConditions){
//                        System.out.println(json);
                        String add = "/condition_" + (String) json.get("type");
                        String name1 = add + "/lastHeartbeatTime";
                        String name2 = add + "/lastTransitionTime";
                        String name3 = add + "/message";
                        String name4 = add + "/reason";
                        String name5 = add + "/status";
                        resource.addProperty(model.createProperty(names, add), model.createResource()
                                .addProperty(model.createProperty(names, name1),
                                        (String) json.get("lastHeartbeatTime"))
                                .addProperty(model.createProperty(names,name2),
                                        (String) json.get("lastTransitionTime"))
                                .addProperty(model.createProperty(names,name3),
                                        (String) json.get("message"))
                                .addProperty(model.createProperty(names,name4),
                                        (String) json.get("reason"))
                                .addProperty(model.createProperty(names,name5),
                                        (String) json.get("status")));
                    }
                    //存储fuseki
                    model.write(System.out, "RDF/XML-ABBREV");
                    DataAccessor.getInstance().add(model);
                    model.write(System.out, "N-TRIPLE");

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeEnvironment(String address){
        try {
            System.out.println("storing env");
            String envName = "http://environment/"+address;
            System.out.println("env name: "+envName);
            Model model = ModelFactory.createDefaultModel();
            try {
                Resource res = model.createResource(envName);
                res.addProperty(model.createProperty(envName+"/name"), "env");
                //存储fuseki
                DataAccessor.getInstance().add(model);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean storeEnvRelation(String address){
        try {
            String envName = "http://environment/"+address;
            RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                    .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
            Query query = QueryFactory.create("SELECT ?p ?o " +
                    "WHERE { <http://backup/"+address+"> ?p ?o }");
            try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
                QueryExecution qExec = conn.query(query) ;
                ResultSet rs = qExec.execSelect() ;
                while(rs.hasNext()) {
                    QuerySolution qs = rs.next() ;
                    String subject = qs.get("o").toString();
                    System.out.println("adding env, server name: " + subject);
                    String urlNode = "http://server/"+address+"/"+subject;

                    String addRelation = "PREFIX j0:<"+envName+"/>\n" +
                            "INSERT DATA{\n" +
                            "<"+envName+"> j0:has "+"<"+urlNode+">\n" +
                            "}";
                    RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                            .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
                    credsProvider.setCredentials(AuthScope.ANY, credentials);
                    HttpClient httpclient = HttpClients.custom()
                            .setDefaultCredentialsProvider(credsProvider)
                            .build();
                    HttpOp.setDefaultHttpClient(httpclient);
                    builderAddRelation.httpClient(httpclient);

                    try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                        connAddRelation.update(addRelation);
                    }
                }
                qExec.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeMasterNode(String masterName){
        // get address
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        String jsonString = getData(httpUrl);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
        // save master url
        String urlMasterNode = "http://server/"+address+"/"+masterName;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        // execute query, get all nodes
        Query query = QueryFactory.create("SELECT ?p ?o " +
                "WHERE { <http://backup/"+address+"> ?p ?o }");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query) ;
            ResultSet rs = qExec.execSelect() ;
            while(rs.hasNext()) {
                QuerySolution qs = rs.next() ;
                String subject = qs.get("o").toString();
                System.out.println("server name: " + subject);
                // add relations between master and other nodes
                if(!subject.equals(masterName)){
                    String urlNode = "http://server/"+address+"/"+subject;

                    String addRelation = "PREFIX j0:<"+urlMasterNode+"/>\n" +
                            "INSERT DATA{\n" +
                            "<"+urlMasterNode+"> j0:manage "+"<"+urlNode+">\n" +
                            "}";
                    RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                            .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
                    credsProvider.setCredentials(AuthScope.ANY, credentials);
                    HttpClient httpclient = HttpClients.custom()
                            .setDefaultCredentialsProvider(credsProvider)
                            .build();
                    HttpOp.setDefaultHttpClient(httpclient);
                    builderAddRelation.httpClient(httpclient);

                    try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build()) {
                        connAddRelation.update(addRelation);
                    }
                }
            }
            qExec.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private static MongoDriver mongoDriver = new MongoDriver();

    public static boolean storePodName(String podName){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_pods/"+podName;
        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            for (int i=0;i<jsonArray.size();i++){
                try {
                    JSONObject jMetaData = jsonArray.getJSONObject(i).getJSONObject("metadata");
                    JSONObject jSpec = jsonArray.getJSONObject(i).getJSONObject("spec");
                    JSONObject jStatus = jsonArray.getJSONObject(i).getJSONObject("status");

                    Model model = ModelFactory.createDefaultModel();
                    String names = "http://pods/" + address + "/"+podName+"/"+(String) jMetaData.get("name");
                    Resource resource = model.createResource(names);
                    String namePod = names;
                    String temp = "http://pods/" + address + "/"+podName;
                    resource.addProperty(model.createProperty(temp,"/name"), (String) jMetaData.get("name"));
                    resource.addProperty(model.createProperty(names, "/namespace"), (String) jMetaData.get("namespace"));
                    resource.addProperty(model.createProperty(names, "/labels"),
                            model.createResource().addProperty(model.createProperty(names, "/labels/name"), (String) jMetaData.getJSONObject("labels").get("name"))
                                    .addProperty(model.createProperty(names, "/labels/pod-template-hash"), (String) jMetaData.getJSONObject("labels").get("pod-template-hash")));
                    resource.addProperty(model.createProperty(names, "/nodeName"), (String) jSpec.get("nodeName"));
                    resource.addProperty(model.createProperty(names, "/nodeSelector"),
                            model.createResource().addProperty(model.createProperty(names,"nodeSelector/beta.kubernetes.io-os"), (String) jSpec.getJSONObject("nodeSelector").get("beta.kubernetes.io/os")));

                    resource.addProperty(model.createProperty(names, "/hostIP"), (String) jStatus.get("hostIP"));
                    resource.addProperty(model.createProperty(names, "/podIP"), (String) jStatus.get("podIP"));
                    resource.addProperty(model.createProperty(names, "/qosClass"), (String) jStatus.get("qosClass"));
                    resource.addProperty(model.createProperty(names, "/startTime"), (String) jStatus.get("startTime"));
                    resource.addProperty(model.createProperty(names, "/phase"), (String) jStatus.get("phase"));

                    //以上POD信息完整
                    //以下CONTAINER
                    JSONArray jCont = jSpec.getJSONArray("containers");
                    JSONObject jContainer = jCont.getJSONObject(0);
                    JSONObject jContainerStatus = jsonArray.getJSONObject(i).getJSONObject("status").getJSONArray("containerStatuses").getJSONObject(0);
                    String container_node_name = (String)jContainer.get("name");

                    Model modelContainer = ModelFactory.createDefaultModel();
                    names = "http://containers/" + address + "/"+podName+"/"+ container_node_name;
                    Resource resourceContainer = modelContainer.createResource(names);
                    resourceContainer.addProperty(modelContainer.createProperty("http://containers/" + address + "/"+podName+"/"+"name"), container_node_name);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/image"),(String)jContainer.get("image"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/ports"),
                            modelContainer.createResource().addProperty(modelContainer.createProperty(names,"/ports/containerPort"),(String)jContainer.getJSONArray("ports").getJSONObject(0).get("containerPort").toString())
                                    .addProperty(modelContainer.createProperty(names,"/ports/protocol"),(String)jContainer.getJSONArray("ports").getJSONObject(0).get("protocol")));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/containerID"),(String)jContainerStatus.get("containerID"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/imageID"),(String)jContainerStatus.get("imageID"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/ready"),(String) jContainerStatus.get("ready").toString());
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/restartCount"),(String)jContainerStatus.get("restartCount").toString());
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/ports"),
                            modelContainer.createResource().addProperty(modelContainer.createProperty(names,"/ports/curstate"),(String)jContainerStatus.getJSONObject("state").keySet().toArray()[0])
                                    .addProperty(modelContainer.createProperty(names,"/ports/startedAt"),(String)jContainerStatus.getJSONObject("state").getJSONObject((String)jContainerStatus.getJSONObject("state").keySet().toArray()[0]).get("startedAt")));
                    resource.addProperty(model.createProperty(namePod,"/contains"),resourceContainer);

                    String[] query = {"avg(rate (container_cpu_usage_seconds_total{image!=\"\",container_name!=\"POD\",namespace=~\"sock-shop\",pod_name=~\""+container_node_name+"-[0-9A-Za-z]{3,}.*\"}[5m]))",
                            "avg(container_memory_usage_bytes{image!=\"\",container_name!=\"POD\",namespace=~\"sock-shop\",pod_name=~\""+container_node_name+"-[0-9A-Za-z]{3,}.*\"})",
                            "sum (rate (container_network_receive_bytes_total{image!=\"\",namespace=~\"sock-shop\",pod_name=~\""+container_node_name+"-[0-9A-Za-z]{3,}.*\"}[5m]))",
                            "sum (rate (container_network_transmit_bytes_total{image!=\"\",namespace=~\"sock-shop\",pod_name=~\""+container_node_name+"-[0-9A-Za-z]{3,}.*\"}[5m]))",
                            "sum (rate (container_network_receive_packets_total{image!=\"\",namespace=~\"sock-shop\",pod_name=~\""+container_node_name+"-[0-9A-Za-z]{3,}.*\"}[5m]))",
                            "sum (rate (container_network_transmit_packets_total{image!=\"\",namespace=~\"sock-shop\",pod_name=~\""+container_node_name+"-[0-9A-Za-z]{3,}.*\"}[5m]))"};
                    String[] name = {"CPU_Usage", "MEM_Usage", "Network_Input_Bytes", "Network_Output_Bytes", "Network_Input_Packets", "Network_Output_Packets"};

                    // save query statements to mongo
                    for (int j = 0; j < 6; j++) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", "container/"+container_node_name+"/"+name[j]);
                        map.put("sql", query[j]);
                        mongoDriver.save_data(map);
                    }

                    Model modelContainerStorage = ModelFactory.createDefaultModel();
//                    String nameStorage = names+"/containerStorage";
                    String nameStorage1 = names+"/CPU_Usage";
                    String nameStorage2 = names+"/MEM_Usage";
                    Resource resourceStorage1 = modelContainerStorage.createResource(nameStorage1);
                    resourceStorage1.addProperty(modelContainerStorage.createProperty(nameStorage1,"/query"), query[0]);
                    Resource resourceStorage2 = modelContainerStorage.createResource(nameStorage2);
                    resourceStorage2.addProperty(modelContainerStorage.createProperty(nameStorage2,"/query"), query[1]);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceStorage1);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceStorage2);

                    Model modelContainerNetwork = ModelFactory.createDefaultModel();
                    String nameNetwork1 = names+"/Network_Input_Bytes";
                    String nameNetwork2 = names+"/Network_Output_Bytes";
                    String nameNetwork3 = names+"/Network_Input_Packets";
                    String nameNetwork4 = names+"/Network_Output_Packets";
                    Resource resourceNetwork1 = modelContainerNetwork.createResource(nameNetwork1);
                    Resource resourceNetwork2 = modelContainerNetwork.createResource(nameNetwork2);
                    Resource resourceNetwork3 = modelContainerNetwork.createResource(nameNetwork3);
                    Resource resourceNetwork4 = modelContainerNetwork.createResource(nameNetwork4);
                    resourceNetwork1.addProperty(modelContainerNetwork.createProperty(nameNetwork1,"/query"), query[2]);
                    resourceNetwork2.addProperty(modelContainerNetwork.createProperty(nameNetwork2,"/query"), query[3]);
                    resourceNetwork3.addProperty(modelContainerNetwork.createProperty(nameNetwork3,"/query"), query[4]);
                    resourceNetwork4.addProperty(modelContainerNetwork.createProperty(nameNetwork4,"/query"), query[5]);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceNetwork1);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceNetwork2);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceNetwork3);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceNetwork4);

                    //以上CONTAINER信息完整
                    //以下添加关系

                    model.write(System.out, "RDF/XML-ABBREV");
                    modelContainer.write(System.out, "RDF/XML-ABBREV");
                    model.write(System.out, "N-TRIPLE");
                    modelContainer.write(System.out, "N-TRIPLE");
                    //存储fuseki
                    DataAccessor.getInstance().add(model);
                    DataAccessor.getInstance().add(modelContainer);
                    DataAccessor.getInstance().add(modelContainerStorage);
                    DataAccessor.getInstance().add(modelContainerNetwork);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            //model.write(System.out, "N-TRIPLE");
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeServiceName(String serviceName){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_svc/"+serviceName;
        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            for (int i=0;i<jsonArray.size();i++) {
                try {
                    JSONObject jMetaData = jsonArray.getJSONObject(i).getJSONObject("metadata");
                    JSONObject jSpec = jsonArray.getJSONObject(i).getJSONObject("spec");
                    JSONObject jStatus = jsonArray.getJSONObject(i).getJSONObject("status");
                    String service_node_name = (String) jMetaData.get("name");

                    Model model = ModelFactory.createDefaultModel();
                    String names = "http://services/" + address + "/" + serviceName + "/" + service_node_name;
                    Resource resource = model.createResource(names);
                    resource.addProperty(model.createProperty("http://services/" + address + "/" + serviceName+"/name"),service_node_name);
                    resource.addProperty(model.createProperty(names ,"/annotations"),model.createResource()
                            .addProperty(model.createProperty(names,"/annotations/kubectl.kubernetes.io/last-applied-configuration"),
                                    (String) jMetaData.getJSONObject("annotations").get("kubectl.kubernetes.io/last-applied-configuration")));
                    resource.addProperty(model.createProperty(names, "/creationTimestamp"), (String) jMetaData.get("creationTimestamp"));
                    resource.addProperty(model.createProperty(names, "/namespace"), (String) jMetaData.get("namespace"));
                    resource.addProperty(model.createProperty(names, "/resourceVersion"), (String) jMetaData.get("resourceVersion"));
                    resource.addProperty(model.createProperty(names, "/labels"),model.createResource()
                            .addProperty(model.createProperty(names,"/labels/name"),
                                    (String) jMetaData.getJSONObject("labels").get("name")));
                    resource.addProperty(model.createProperty(names, "/clusterIP"), (String) jSpec.get("clusterIP"));
                    resource.addProperty(model.createProperty(names, "/type"), (String) jSpec.get("type"));
                    resource.addProperty(model.createProperty(names, "/selector"),model.createResource()
                            .addProperty(model.createProperty(names,"/selector/name"),
                                    (String) jSpec.getJSONObject("selector").get("name")));
                    resource.addProperty(model.createProperty(names, "/ports"),model.createResource()
                            .addProperty(model.createProperty(names,"/ports/port"), (String)jSpec.getJSONArray("ports").getJSONObject(0).get("port").toString())
                            .addProperty(model.createProperty(names,"/ports/protocol"), (String)jSpec.getJSONArray("ports").getJSONObject(0).get("protocol"))
                            .addProperty(model.createProperty(names,"/ports/targetPort"), (String)jSpec.getJSONArray("ports").getJSONObject(0).get("targetPort").toString()));
                    //存储fuseki
                    Model modelService = ModelFactory.createDefaultModel();
                    Model modelDatabase = ModelFactory.createDefaultModel();
                    if (!((String) jMetaData.get("name")).contains("db")){
                        String[] name = {"latency", "success_rate"};
                        String[] query = {"sum(rate(request_duration_seconds_sum{service=\""+service_node_name+"\"}[1m])) / sum(rate(request_duration_seconds_count{service=\""+service_node_name+"\"}[1m]))", "sum(rate(request_duration_seconds_count{service=\""+service_node_name+"\",status_code=~\"2..\",route!=\"metrics\"}[1m]))"};
//                        String nameService = names+"/serviceProfile";
                        String nameService1 = names+"/latency";
                        String nameService2 = names+"/success_rate";
                        Resource resourceService1 = modelService.createResource(nameService1);
                        Resource resourceService2 = modelService.createResource(nameService2);
                        resourceService1.addProperty(modelService.createProperty(nameService1,"/query"), query[0]);
                        resourceService2.addProperty(modelService.createProperty(nameService2,"/query"),query[1]);
                        resource.addProperty(model.createProperty(names,"/profile"),resourceService1);
                        resource.addProperty(model.createProperty(names,"/profile"),resourceService2);
                        // save query statements to mongo
                        for (int j = 0; j < 2; j++) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("type", "service/"+service_node_name+"/"+name[j]);
                            map.put("sql", query[j]);
                            mongoDriver.save_data(map);
                        }
                    }else{
                        String[] name = {"throughput", "latency"};
                        String[] query = {"sum(rate(request_duration_seconds_sum{service=\""+service_node_name+"\"}[1m])) / sum(rate(request_duration_seconds_count{service=\""+service_node_name+"\"}[1m]))", "sum(rate(request_duration_seconds_count{service=\""+service_node_name+"\",status_code=~\"2..\",route!=\"metrics\"}[1m]))"};
                        String nameDatabase1 = names+"/throughput";
                        String nameDatabase2 = names+"/latency";
                        Resource resourceDatabase1 = modelDatabase.createResource(nameDatabase1);
                        Resource resourceDatabase2 = modelDatabase.createResource(nameDatabase2);
                        resourceDatabase1.addProperty(modelDatabase.createProperty(nameDatabase1,"/query"), query[0]);
                        resourceDatabase2.addProperty(modelDatabase.createProperty(nameDatabase2,"/query"), query[1]);
                        resource.addProperty(model.createProperty(names,"/profile"),resourceDatabase1);
                        resource.addProperty(model.createProperty(names,"/profile"),resourceDatabase2);// save query statements to mongo
                        for (int j = 0; j < 2; j++) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("type", "service/"+service_node_name+"/"+name[j]);
                            map.put("sql", query[j]);
                            mongoDriver.save_data(map);
                        }
                    }

                    model.write(System.out, "RDF/XML-ABBREV");
                    DataAccessor.getInstance().add(model);
                    DataAccessor.getInstance().add(modelService);
                    DataAccessor.getInstance().add(modelDatabase);
                    model.write(System.out, "N-TRIPLE");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean podToService(String address, String namespace) {
        String urlPod = "http://pods/" + address + "/" + namespace + "/";
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");

        Query query = QueryFactory.create("PREFIX j0:<" + urlPod + ">\n" +
                "SELECT ?o WHERE {\n" +
                "\t?s j0:name ?o\n" +
                "}");
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            QueryExecution qExec = conn.query(query);
            ResultSet rs = qExec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String subject = qs.get("o").toString();
                System.out.println("Subject1: " + subject);
                String name = urlPod + subject + "/labels/";

                query = QueryFactory.create("PREFIX j0:<" + name + ">\n" +
                        "SELECT ?o WHERE {\n" +
                        "\t?s j0:name ?o\n" +
                        "}");

                try (RDFConnectionFuseki connServiceName = (RDFConnectionFuseki) builder.build()) {

                    QueryExecution qExecServiceName = connServiceName.query(query);
                    ResultSet rsServiceName = qExecServiceName.execSelect();
                    while (rsServiceName.hasNext()) {
                        QuerySolution qsServiceName = rsServiceName.next();
                        String subjectServiceName = qsServiceName.get("o").toString();
                        String urlService = "http://services/" + address + "/" + namespace + "/" + subjectServiceName;
                        String temp = subject.replace("/","");
                        System.out.println("**"+temp);

                        String addRelation = "PREFIX j0:<" + urlPod + subject + "/>\n" +
                                "INSERT DATA{\n" +
                                "<" + urlPod + temp + ">  j0:provides " + "<" + urlService + ">\n" +
                                "}";
                        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                                .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
                        credsProvider.setCredentials(AuthScope.ANY, credentials);
                        HttpClient httpclient = HttpClients.custom()
                                .setDefaultCredentialsProvider(credsProvider)
                                .build();
                        HttpOp.setDefaultHttpClient(httpclient);
                        builderAddRelation.httpClient(httpclient);

                        try (RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki) builderAddRelation.build()) {
                            connAddRelation.update(addRelation);
                        }

                    }
                    qExecServiceName.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            qExec.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean podToServer(String address,String namespace){
        String urlPod = "http://pods/"+address+"/"+namespace+"/";
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");

        Query query = QueryFactory.create("PREFIX j0:<"+urlPod+">\n" +
                "SELECT ?o WHERE {\n" +
                "\t?s j0:name ?o\n" +
                "}");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query) ;
            ResultSet rs = qExec.execSelect() ;
            while(rs.hasNext()) {
                QuerySolution qs = rs.next() ;
                String subject = qs.get("o").toString();
                System.out.println("Subject: " + subject);
                String name = urlPod+subject+"/";

//                查找当前Pod所在的node name
                query = QueryFactory.create("PREFIX j0:<"+name+">\n" +
                        "SELECT ?o WHERE {\n" +
                        "\t?s j0:nodeName ?o\n" +
                        "}");

                try ( RDFConnectionFuseki connServiceName = (RDFConnectionFuseki)builder.build() ) {

                    QueryExecution qExecServiceName = connServiceName.query(query);
                    ResultSet rsServiceName = qExecServiceName.execSelect();
                    while (rsServiceName.hasNext()) {
                        QuerySolution qsServiceName = rsServiceName.next();
                        String nodeName = qsServiceName.get("o").toString();
                        String urlNode = "http://server/"+address+"/"+ nodeName;
                        System.out.println(urlNode);

                        String addRelation = "PREFIX j0:<"+urlPod+subject+"/>\n" +
                                "INSERT DATA{\n" +
                                "<"+urlPod+subject+">  j0:deployed_in "+"<"+urlNode+">\n" +
                                "}";
                        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                                .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
                        credsProvider.setCredentials(AuthScope.ANY, credentials);
                        HttpClient httpclient = HttpClients.custom()
                                .setDefaultCredentialsProvider(credsProvider)
                                .build();
                        HttpOp.setDefaultHttpClient(httpclient);
                        builderAddRelation.httpClient(httpclient);

                        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                            connAddRelation.update(addRelation);
                        }

                    }
                    qExecServiceName.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            qExec.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storePrometheus(String url, String namespace, String serviceName){
        try {
            System.out.println("storing prometheus...");
            String proName = "http://prometheus/"+url;
            System.out.println("prometheus name: "+proName);
            Model model = ModelFactory.createDefaultModel();
            try {
                Resource res = model.createResource(proName);
                res.addProperty(model.createProperty(proName+"/url"), url);
                //存储fuseki
                DataAccessor.getInstance().add(model);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
            String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_svc/"+namespace;
            try {
                String jsonString = getData(httpUrl);
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
                RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                        .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
                String response_time = "http://services/" + address + "/" + namespace + "/" + serviceName + "/response_time";
                String proRelation = "PREFIX j0:<"+response_time+"/>\n" +
                        "INSERT DATA{\n" +
                        "<"+response_time+"> j0:stored_in "+"<"+proName+">\n" +
                        "}";
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
                credsProvider.setCredentials(AuthScope.ANY, credentials);
                HttpClient httpclient = HttpClients.custom()
                        .setDefaultCredentialsProvider(credsProvider)
                        .build();
                HttpOp.setDefaultHttpClient(httpclient);
                builder.httpClient(httpclient);

                try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builder.build() ) {
                    connAddRelation.update(proRelation);
                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        finally {
            System.out.println("finish storing prometheus");
        }
        return true;
    }

    public static Map<String, Object> getNamespace(String urlNode){
        String[] l = urlNode.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", urlNode);
        node.put("name", l[l.length-1]);
        node.put("type", "namespace");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+urlNode+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("name")){
                    pro.put("name", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getTimestamp(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "timestamp");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("attr")){
                    pro.put("attr", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getEnv(String urlNode){
        String[] l = urlNode.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", urlNode);
        node.put("name", l[l.length-1]);
        node.put("type", "environment");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+urlNode+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("name")){
                    pro.put("name", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getServer(String urlNode){
        String[] l = urlNode.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", urlNode);
        node.put("name", l[l.length-1]);
        if(urlNode.contains("192.168.199.191"))
            node.put("type", "masterServer");
        else
            node.put("type", "server");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+urlNode+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            // error
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
//                System.out.println(p+", "+o);
                if(p.equals("creationTimestamp")){
                    pro.put("creationTimestamp", o);
                }
                else if(p.equals("podCIDR")){
                    pro.put("podCIDR", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getService(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "service");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("creationTimestamp")){
                    pro.put("creationTimestamp", o);
                }
                else if(p.equals("resourceVersion")){
                    pro.put("resourceVersion", o);
                }
                else if(p.equals("namespace")){
                    node.put("namespace", o);
                }
                else if(p.equals("clusterIP")){
                    pro.put("clusterIP", o);
                }
                else if(p.equals("type")){
                    pro.put("service_type", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getPropertyNodes(String url, String type){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", type);
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("query")){
                    pro.put("query", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getEventNodes(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "event");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if (p.equals("type")){
                    node.put("type", o);
                    break;
                }
                pro.put(p, o);
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getPod(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "pod");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next();
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length - 1];
                String o = q.get("o").toString();
                if (p.equals("deployed_in")) {
                    pro.put("deployed_in", o);
                } else if (p.equals("contains")) {
                    pro.put("contains", o);
                } else if (p.equals("qosClass")) {
                    pro.put("qosClass", o);
                } else if (p.equals("podIP")) {
                    pro.put("podIP", o);
                } else if (p.equals("startTime")) {
                    pro.put("startTime", o);
                } else if (p.equals("hostIP")) {
                    pro.put("hostIP", o);
                } else if (p.equals("phase")) {
                    pro.put("phase", o);
                } else if (p.equals("namespace")) {
                    pro.put("namespace", o);
                } else if (p.equals("nodeName")) {
                    pro.put("nodeName", o);
                }
            }
            qE.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getContainer(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "container");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("restartCount")){
                    pro.put("restartCount", o);
                }
                else if(p.equals("image")){
                    pro.put("image", o);
                }
                else if(p.equals("ready")){
                    pro.put("ready", o);
                }
                else if(p.equals("containerID")){
                    pro.put("containerID", o);
                }
                else if(p.equals("imageID")){
                    pro.put("imageID", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static List<Map<String, Object>> getLink(String url, String linkType){
        List<Map<String, Object>> list = new ArrayList<>();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?s ?o WHERE {\n" +
                "\t?s <"+url+"/"+linkType+"> ?o\n" +
                "}");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {

            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                Map<String, Object> link = new HashMap<>();
                QuerySolution q = rs.next() ;
                link.put("sid", q.get("s").toString());
                link.put("tid", q.get("o").toString());
                link.put("name", linkType);
                link.put("type", linkType);
                list.add(link);
            }
            qE.close();
        }
        System.out.println(list);
        return list;
    }

    public static boolean judgeExist(String url){
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");

        Query query = QueryFactory.create("SELECT distinct ?s WHERE {\n" +
                "\t?s ?p ?o\n" +
                "}");

        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query);
            ResultSet rs = qExec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String subject = qs.get("s").toString();
                if (subject.equals(url)){
                    return false;
                }
            }
            qExec.close();
        }
        return true;
    }

    public static boolean deleteOneLink(String sid, String tid){
        try {
            RDFConnectionRemoteBuilder builder2 = RDFConnectionFuseki.create()
                    .destination(globalvalue.fusekiapi+":3030/DevKGData/update");

            String delete = "DELETE WHERE\n" +
                    "{\n" +
                    "<"+sid+"> ?p <"+tid+"> .\n" +
                    "}";
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
            credsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();
            HttpOp.setDefaultHttpClient(httpclient);
            builder2.httpClient(httpclient);
            try ( RDFConnectionFuseki connUpdate = (RDFConnectionFuseki)builder2.build() ) {
                connUpdate.update(delete);
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteOneNode(String url){
        try {
            RDFConnectionRemoteBuilder builder2 = RDFConnectionFuseki.create()
                    .destination(globalvalue.fusekiapi+":3030/DevKGData/update");
            String delete = "DELETE WHERE\n" +
                    "{\n" +
                    "<"+url+"> ?p ?o .\n" +
                    "}";
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
            credsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();
            HttpOp.setDefaultHttpClient(httpclient);
            builder2.httpClient(httpclient);
            try ( RDFConnectionFuseki connUpdate = (RDFConnectionFuseki)builder2.build() ) {
                connUpdate.update(delete);
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String findEvent(String full_url){
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(globalvalue.fusekiapi+":3030/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?s ?p ?o WHERE {\n" +
                "\t<"+full_url+"> ?p ?o\n" +
                "}");
        String eventID = "";
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                if(p.equals("full_url")){
                    eventID = q.get("s").toString();
                }
            }
            qE.close();
        }
        System.out.println(eventID);
        return eventID;
    }

    public static ArrayList getResourcesWithQuery(){
        ArrayList result = new ArrayList();
        Model model = DataAccessor.getInstance().getModel();
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            if (r.hasProperty(model.createProperty(r.toString()+"/query"))){
                result.add(r);
            }
        }
        //System.out.println(result);
        return result;
    }

    //将event和实体的属性对应起来的接口（添加事件和指标之间的关联度）
    public static void addLinkEvent2S(){
        SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        Date start = new Date();
        Date end = new Date();
        try {
            start = DateFormat.parse("2019-10-20 00:00:00");
            end = DateFormat.parse("2019-10-20 23:59:59");
        } catch(ParseException px) {
            px.printStackTrace();
        }
        Model model = DataAccessor.getInstance().getModel();
        ArrayList<Resource> resources = getResourcesWithQuery();
        //受到时区的影响
        Map dates = getEventInFuseki(start, end);
        List times = new ArrayList<>();
        for (Date i:(ArrayList<Date>)dates.get("Date")
        ) {
            times.add(i.getTime()/1000);
        }
        Collections.sort(times);
        StringBuffer stringBuffer = new StringBuffer();
        for (Resource i:resources
        ) {
            Statement statement = i.getProperty(model.createProperty(i.toString()+"/query"));
            //System.out.println(start.getTime()/1000 + " " + end.getTime()/1000);

            JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),start.getTime()/1000 + "", end.getTime()/1000 + "");
            //JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),times.get(0)+"", times.get(times.size()-1)+"");
            if (proInfor == null)continue;
            List timeList = new ArrayList();
            for (Object j:times
            ) {
                for (Object k:proInfor
                ) {
                    if ((Long)j <= ((JSONArray)k).getLong(0)){
                        if (!timeList.contains(((JSONArray)k).getLong(0))){
                            timeList.add(((JSONArray)k).getLong(0));
                        }
                        break;
                    }
                }
            }
            String[] strings = i.toString().split("/");
            System.out.println(strings[strings.length-1]);
            System.out.println(timeList);
            System.out.println(proInfor);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file1",timeList);
            jsonObject.put("file2", proInfor);
            String re =  util.HttpPostUtil.postData(jsonObject.toJSONString());
            System.out.println(re);
            if (re != null){
                for (String ev:(ArrayList<String>)dates.get("Event")
                ) {
                    addCorrelation(ev, i.toString(), re);
                }
            }
        }
    }


    //查询指定范围内发生的时间 只有 HW 事件
    public static Map<String, ArrayList> getEventInFuseki(Date startTime,Date endTime)
    {
        SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        String start = DateFormat.format(startTime);
        String end = DateFormat.format(endTime);
        Map result = new HashMap<>();
        result.put("Date", new ArrayList<>());
        result.put("Event", new ArrayList<>());
        Model model = DataAccessor.getInstance().getModel();
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            if (Pattern.matches(".*event.*HW.*", r.toString()))
            {
                Statement statement =  r.getProperty(model.createProperty(r.toString()+"/starts_at"));
                if (statement == null) continue;
                String timeProperty=statement.getResource().toString();
                //截取出时间字符串，去掉中间的“-”
                int length=timeProperty.length();
                String time=timeProperty.substring(length-19,length-9)+" "+timeProperty.substring(length-8);
                if(time.compareTo(start)>=0&&time.compareTo(end)<=0)
                {
                    try {
                        Date dateResult = DateFormat.parse(time);
                        ((ArrayList)result.get("Date")).add(dateResult);
                        ((ArrayList)result.get("Event")).add(r.toString());
                    }catch(ParseException px) {
                        px.printStackTrace();
                    }
                }
            }
        }
        System.out.println(result);
        return result;
    }

    public static boolean addCorrelation(String fromUrl, String toUrl, String influence){
        int last= toUrl.lastIndexOf("/");
        String temp=toUrl.substring(0,last);
        int target=temp.lastIndexOf("/");
        String index=toUrl.substring(target+1);
        String result=index.replace('/','_');
        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
                "INSERT DATA{\n" +
                "<"+fromUrl+"> j0:influence_"+result+"<"+toUrl+">\n" +
                "}";
        String setInfluenceValue = "PREFIX j0:<"+fromUrl+"/influence_"+result+"/>\n" +
                "INSERT DATA{\n" +
                "<"+fromUrl+"/influence_"+result+"> j0:value\""+influence+"\"\n" +
                "}";
        System.out.println(addRelation);
        System.out.println(setInfluenceValue);
        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                                .destination("http://10.60.38.173:3030/DevKGData/update");
                //.destination("http://localhost:3030/gundam/update");
        //        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        //        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
        //        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom()
                //                .setDefaultCredentialsProvider(credsProvider)
                .build();
        HttpOp.setDefaultHttpClient(httpclient);
        builderAddRelation.httpClient(httpclient);
        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
            connAddRelation.update(addRelation);
            connAddRelation.update(setInfluenceValue);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }


    //供算法导出数据使用
    public static void getDate(int month, int day){
        SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        Date start = new Date();
        Date end = new Date();
        try {
            start = DateFormat.parse("2019-"+month+"-"+day+" 00:00:00");
            end = DateFormat.parse("2019-"+month+"-"+day+" 23:59:59");
        } catch(ParseException px) {
            px.printStackTrace();
        }
        Model model = DataAccessor.getInstance().getModel();
        ArrayList<Resource> resources = getResourcesWithQuery();
        //受到时区的影响
        Map dates = getEventInFuseki(start, end);
        List times = new ArrayList<>();
        for (Date i:(ArrayList<Date>)dates.get("Date")
        ) {
            times.add(i.getTime()/1000);
        }
        Collections.sort(times);
        StringBuffer stringBuffer = new StringBuffer();
        for (Resource i:resources
        ) {
            Statement statement = i.getProperty(model.createProperty(i.toString()+"/query"));
            //System.out.println(start.getTime()/1000 + " " + end.getTime()/1000);

            JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),start.getTime()/1000 + "", end.getTime()/1000 + "");
            //JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),times.get(0)+"", times.get(times.size()-1)+"");
            if (proInfor == null)continue;
            List timeList = new ArrayList();
            for (Object j:times
            ) {
                for (Object k:proInfor
                ) {
                    if ((Long)j <= ((JSONArray)k).getLong(0)){
                        if (!timeList.contains(((JSONArray)k).getLong(0))){
                            timeList.add(((JSONArray)k).getLong(0));
                        }
                        break;
                    }
                }
            }
            String[] strings = i.toString().split("/");
            System.out.println(strings[strings.length-1]);
            System.out.println(timeList);
            System.out.println(proInfor);
            stringBuffer.append(strings[strings.length-1]);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file1",timeList);
            jsonObject.put("file2", proInfor);
            stringBuffer.append("\r\n");
            stringBuffer.append(jsonObject.toString());
            stringBuffer.append("\r\n");
        }
        if (stringBuffer.length() == 0){
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream("/Users/jiang/data/data"+month+"-"+day+".txt");
            fos.write(stringBuffer.toString().getBytes());
            fos.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //向算法部分发送数据，返回一个String、两个时间序列
    public static void getClusterResult(){
        SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        Date start = new Date();
        Date end = new Date();
        try {
            start = DateFormat.parse("2019-11-18 00:00:00");
            end = DateFormat.parse("2019-11-18 23:59:59");
        } catch(ParseException px) {
            px.printStackTrace();
        }
        Model model = DataAccessor.getInstance().getModel();
        ArrayList<Resource> resources = getResourcesWithQuery();
        //受到时区的影响
        Map dates = getEventInFuseki(start, end);
        List times = new ArrayList<>();
        for (Date i:(ArrayList<Date>)dates.get("Date")
        ) {
            times.add(i.getTime()/1000);
        }
        Collections.sort(times);
        StringBuffer stringBuffer = new StringBuffer();
        for (Resource i:resources
        ) {
            Statement statement = i.getProperty(model.createProperty(i.toString()+"/query"));
            JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),start.getTime()/1000 + "", end.getTime()/1000 + "");
            if (proInfor == null)continue;
            List timeList = new ArrayList();
            for (Object j:times
            ) {
                for (Object k:proInfor
                ) {
                    if ((Long)j <= ((JSONArray)k).getLong(0)){
                        if (!timeList.contains(((JSONArray)k).getLong(0))){
                            timeList.add(((JSONArray)k).getLong(0));
                        }
                        break;
                    }
                }
            }
            String[] strings = i.toString().split("/");
            System.out.println(strings[strings.length-1]);
            System.out.println(timeList);
            System.out.println(proInfor);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file4", 1);
            jsonObject.put("file3", strings[strings.length-1]);
            jsonObject.put("file1",timeList);
            jsonObject.put("file2", proInfor);

            String re =  util.HttpPostUtil.postData(jsonObject.toJSONString());
            System.out.println(re);
            if (re != null){
                JSONObject jsonRe = JSON.parseObject(re);
//                String correlation = jsonRe.getString("Correlation");
//                List<List> SST = JSONArray.parseArray(jsonRe.getString("SST"), List.class);
//                List<List> alarm = JSONArray.parseArray(jsonRe.getString("Alarm"), List.class);
                saveClusterResult(jsonRe.toJSONString(), strings[strings.length-1]);

            }
        }
    }

    public static void main(String[] args) {
//        for (int i = 30; i <= 31; i++) {
//            getDate(10,i);
//        }
//        for (int i = 1; i <= 14; i++) {
//            getDate(11,i);
//        }


    }
}
